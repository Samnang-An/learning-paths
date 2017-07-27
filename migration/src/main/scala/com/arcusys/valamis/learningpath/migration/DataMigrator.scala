package com.arcusys.valamis.learningpath.migration

import com.arcusys.slick.migration.MigrationSeq
import com.arcusys.slick.migration.dialect.Dialect
import com.arcusys.slick.migration.table.TableMigration
import com.arcusys.valamis.learningpath.migration.schema.old.{FileTableComponent, OldCurriculumTables}
import com.arcusys.valamis.learningpath.migration.schema.old.model.goal.{ActivityGoal, AssignmentGoal, CertificateGoal, CourseGoal, GoalType, PackageGoal, StatementGoal, TrainingEventGoal, Goal => OldGoal, GoalGroup => OldGoalGroup}
import com.arcusys.valamis.learningpath.migration.schema.old.model.{Certificate, PeriodTypes}
import com.arcusys.valamis.learningpath.models.{Goal, _}
import com.arcusys.valamis.learningpath.services.{AssetEntryService, CompanyService, FileStorage}
import com.arcusys.valamis.learningpath.services.impl.tables.{LPMemberTableComponent, LearningPathTables}
import com.arcusys.valamis.learningpath.services.impl.tables.utils.{SlickProfile, TableManagerBase}
import com.arcusys.valamis.learningpath.services.impl.utils.UserStatusUtil
import com.arcusys.valamis.learningpath.tasks.GroupsGoalChecker
import com.arcusys.valamis.learningpath.tasks.LearningPathChecker.getStatus
import com.arcusys.valamis.learningpath.utils.{DbActions, DbActionsSupport}
import com.arcusys.valamis.members.picker.model.{Member, UserMembership}
import com.arcusys.valamis.members.picker.service.LiferayHelper
import com.arcusys.valamis.members.picker.storage.MemberDBActions
import org.apache.commons.logging.Log
import org.joda.time.{DateTime, Period}
import slick.driver.{JdbcDriver, JdbcProfile}

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.Duration
import scala.language.reflectiveCalls
import scala.language.postfixOps

/**
  * Created by pkornilov on 3/22/17.
  */
abstract class DataMigrator(val dbActions: DbActions,
                            liferay: LiferayHelper,
                            val companyService: CompanyService,
                            val assetEntryService: AssetEntryService,
                            val fileStorage: FileStorage,
                            val log: Log)
                           (implicit val executionContext: ExecutionContext) extends SlickProfile
  with OldCurriculumTables
  with FileTableComponent
  with DbActionsSupport
  with MigrationQueries
  with Converters
  with TableManagerBase
  with Helpers {
  self =>


  import profile.api._

  def migrateOldData(): DBIO[Unit] = {
    log.info("Migrating data from Curriculum tables...")
    (for {
      hasOldTables <- hasTable(certificates.baseTableRow.tableName)
      _ <- if (hasOldTables) {
        for {
          oldCertificates <- certificates.result
          _ <- DBIO.seq(oldCertificates map migrateCertificate: _*)
        } yield ()
      } else {
        log.info("There is no old curriculum tables. Nothing to migrate")
        DBIO.successful({})
      }
      nextId <- newTables.learningPathWithoutAutoIncTQ.map(_.id).max.result.map(_.getOrElse(0L) + 1L)
      _ <- setAutoIncForLPTable(nextId)
    } yield ()).transactionally
  }

  private def migrateCertificate(oldCertificate: Certificate): DBIO[_] = {
    implicit val old = oldCertificate
    for {
      lpId <- addLearningPath
      versionId <- addVersion(lpId)
      _ <- newTables.setCurrentVersion(lpId, versionId)
      oldGoalIdToNewGoal <- migrateGoals(versionId)
      _ <- migrateMembers(lpId)
      _ <- migrateUserStates(lpId, versionId, oldGoalIdToNewGoal)
      _ <- createMissingUserStates(oldCertificate, versionId)
      _ <- calculateGoalGroupStates(versionId)
      _ <- calculateLPProgress(versionId)
    } yield ()
  }

  private def addLearningPath(implicit oldCertificate: Certificate): DBIO[Long] = {
    val action = newTables.insertLearningPathWithoutAutoInc(LearningPath(
      id = oldCertificate.id,
      activated = oldCertificate.isActive,
      companyId = oldCertificate.companyId,
      userId = findCreatedUsedId(oldCertificate),
      hasDraft = false,
      currentVersionId = None
    ))

    action map (_ => oldCertificate.id) withErrorLog {
      s"Failed to create learning path for certificate: $oldCertificate"
    }
  }

  private def addVersion(lpId: Long)(implicit oldCertificate: Certificate): DBIO[Long] = {
    val validPeriod =
      PeriodTypes.toJodaPeriod(oldCertificate.validPeriodType, oldCertificate.validPeriod)
    val expiringPeriod = validPeriod map (_ => Period.days(30))

    val lpVersion = LPVersion(
      learningPathId = lpId,
      title = oldCertificate.title,
      description = Option(oldCertificate.description).filterNot(_.isEmpty),
      logo = Option(oldCertificate.logo).filterNot(_.isEmpty) flatMap migrateLogo,
      courseId = oldCertificate.scope,

      validPeriod = validPeriod,
      expiringPeriod = expiringPeriod,

      openBadgesEnabled = oldCertificate.isPublishBadge,
      openBadgesDescription = Option(oldCertificate.shortDescription).filterNot(_.isEmpty),

      published = true,
      createdDate = oldCertificate.createdAt,
      modifiedDate = oldCertificate.activationDate.getOrElse(oldCertificate.createdAt)
    )

    versionDBIO.insert(lpVersion) withErrorLog {
      s"Failed to create version for learning path $lpId; oldCertificate: $oldCertificate"
    }

  }


  private def migrateLogo(logo: String)(implicit oldCertificate: Certificate): Option[String] = {
    val fileName = s"files/${oldCertificate.id}/$logo"
    Await.result(for {
      fileRecord <- db.run(files.filter(f => f.filename === fileName).result.headOption)
      newLogoName <- saveLogoFile(fileRecord)(oldCertificate.companyId)
    } yield newLogoName, Duration.Inf)
  }

  private def migrateGoals(versionId: Long)
                          (implicit oldCertificate: Certificate): DBIO[Map[Long, Goal]] = {
    val action = for {
      oldGoalGroups <- oldGoalsGroupsQ(oldCertificate.id).result //TODO add tests for deleted goal group
      groupIdMap <- DBIO.sequence(oldGoalGroups map migrateGoalGroup(versionId)).map(_.toMap)

      oldGoals <- oldGoalsQ(oldCertificate.id).result //TODO add tests for deleted goals
      oldGoalIdToNewGoal <- DBIO.sequence(oldGoals map migrateGoal(versionId, groupIdMap))
    } yield {
      oldGoalIdToNewGoal collect { case (id, Some(goal)) => (id, goal) } toMap
    }

    action withErrorLog {
      s"Failed to migrate goals for versionId: $versionId; oldCertificate: $oldCertificate"
    }
  }

  private def calculateGoalGroupStates(versionId: Long)(implicit oldCertificate: Certificate) = {
    val now = DateTime.now
    val action = for {
      goalGroups <- dbActions.goalGroupDBIO.getWithGoalInfoByVersionId(versionId)
      userIds <- dbActions.userMemberDBIO.getUserIdsByLearningPathId(oldCertificate.id)
      _ <- DBIO.seq(
        (for {
          goalGroup <- goalGroups
          userId <- userIds
        } yield calculateGoalGroupState(versionId, goalGroup, userId, now)): _*
      )

    } yield ()

    action withErrorLog {
      s"Failed to calculate status for goal groups for versionId: $versionId; oldCertificate: $oldCertificate"
    }
  }

  private def calculateLPProgress(versionId: Long) = {
    for {
      userLPStatuses <- newTables.selectLPNotSuccessStatusesWithoutProgress(versionId)
      _ <- DBIO.seq(userLPStatuses map { userLPStatus =>
        val userId = userLPStatus.userId
        for {
          rootGoals <- goalDBIO.getByVersionIdAndParentGroupId(versionId, parentGroupId = None)
          goalsStatuses <- userGoalStatusDBIO.getByUserIdAndGoalIds(userId, rootGoals.map(_.id))
          _ <- {
            val (_, progress) = getStatus(rootGoals, goalsStatuses)
            val newStatus = userLPStatus.copy(progress = progress)

            userLPStatusDBIO.updateStatus(newStatus)
          }
        } yield ()
      }: _*)
    } yield ()
  }

  private def migrateGoalGroup(versionId: Long)(oldGoalGroup: OldGoalGroup): DBIO[(Long, Long)] = {
    val action = for {
      newId <- goalDBIO.insert(
        oldGoalId = None,
        versionId = versionId,
        groupId = None,
        goalType = GoalTypes.Group,
        indexNumber = oldGoalGroup.arrangementIndex, //TODO check goal group indices
        timeLimit = PeriodTypes.toJodaPeriod(oldGoalGroup.periodType, oldGoalGroup.periodValue),
        optional = false,
        oldGoalGroup.modifiedDate
      )
      _ <- goalGroupDBIO.insert(GoalGroup(
        goalId = newId,
        title = "New group",
        count = Some(oldGoalGroup.count)
      ))
    } yield (oldGoalGroup.id, newId)

    action withErrorLog {
      s"Failed to migrate goal group: $oldGoalGroup; versionId: $versionId"
    }
  }

  private def migrateGoal(versionId: Long, groupIdMap: Map[Long, Long])
                         (oldGoal: CertificateGoal): DBIO[(Long, Option[Goal])] = {
    val newGroupId = oldGoal.groupId flatMap { oldId =>
      //there is possibility, that group for the goal doesn't exist anymore
      groupIdMap.get(oldId)
    }

    val action = for {
      customPart <- getCustomGoalPart(oldGoal)
      newGoal <- customPart.fold[DBIO[Option[Goal]]](DBIO.successful(None)) { custom =>
        goalDBIO.insert(
          oldGoalId = None,
          versionId = versionId,
          groupId = newGroupId,
          goalType = toNewGoalType(oldGoal.goalType),
          indexNumber = oldGoal.arrangementIndex, //TODO check goal indices
          timeLimit = PeriodTypes.toJodaPeriod(oldGoal.periodType, oldGoal.periodValue),
          optional = oldGoal.isOptional,
          oldGoal.modifiedDate
        ) flatMap { newId =>
          migrateCustomGoalPart(custom, newId) map (_ => newId)
        } flatMap { id =>
          goalDBIO.get(id)
        }
      }
    } yield (oldGoal.id, newGoal)

    action withErrorLog {
      s"Failed to migrate goal: $oldGoal; versionId: $versionId"
    }

  }

  private def getCustomGoalPart(oldGoal: CertificateGoal) = {
    oldGoal.goalType match {
      case GoalType.Activity =>
        oldActivityGoalsQ(oldGoal.id).result.headOption
      case GoalType.Assignment =>
        oldAssignmentGoalsQ(oldGoal.id).result.headOption
      case GoalType.Course =>
        oldCourseGoalsQ(oldGoal.id).result.headOption
      case GoalType.Package =>
        oldPackageGoalsQ(oldGoal.id).result.headOption
      case GoalType.Statement =>
        oldStatementGoalsQ(oldGoal.id).result.headOption
      case GoalType.TrainingEvent =>
        oldTrainingEventGoalsQ(oldGoal.id).result.headOption
    }
  }

  private def migrateCustomGoalPart(old: OldGoal, newGoalId: Long): DBIO[Int] = {
    old match {
      case old: ActivityGoal =>
        goalActivityDBIO.insert(GoalLRActivity(
          goalId = newGoalId,
          activityName = old.activityName,
          count = old.count
        ))
      case old: AssignmentGoal =>
        goalAssignmentDBIO.insert(GoalAssignment(
          goalId = newGoalId,
          assignmentId = old.assignmentId
        ))
      case old: CourseGoal =>
        goalCourseDBIO.insert(GoalCourse(
          goalId = newGoalId,
          courseId = old.courseId
        ))
      case old: PackageGoal =>
        goalLessonDBIO.insert(GoalLesson(
          goalId = newGoalId,
          lessonId = old.packageId
        ))
      case old: StatementGoal =>
        goalStatementDBIO.insert(GoalStatement(
          goalId = newGoalId,
          verbId = old.verb,
          objectId = old.obj,
          objectName = "" //object name will be filled in later, because
          //during migration LRS can be unavailable
        ))
      case old: TrainingEventGoal =>
        goalTrainingEventDBIO.insert(GoalTrainingEvent(
          goalId = newGoalId,
          trainingEventId = old.eventId
        ))
    }
  }

  private def migrateMembers(lpId: Long)(implicit oldCertificate: Certificate): DBIO[_] = {
    val action = for {
      oldMembers <- oldMembersQ(oldCertificate.id).result
      _ <- newTables.addMembers(toLPMembers(lpId, oldMembers))
    } yield ()

    action withErrorLog {
      s"Failed to migrate members for certificate: $oldCertificate"
    }
  }

  private def migrateUserStates(lpId: Long,
                                versionId: Long,
                                oldGoalIdToNewGoal: Map[Long, Goal])
                               (implicit oldCertificate: Certificate): DBIO[_] = {
    val validPeriod =
      PeriodTypes.toJodaPeriod(oldCertificate.validPeriodType, oldCertificate.validPeriod)
    for {
      oldCertStates <- oldCertStatesQ(oldCertificate.id).result
      _ <- userLPStatusDBIO.insert(toUserLPStatuses(lpId, versionId, validPeriod, oldCertStates))

      oldGoalStates <- oldGoalStatesQ(oldCertificate.id).result
      _ <- userGoalStatusDBIO.insert(toUserGoalStatuses(oldGoalIdToNewGoal, oldCertStates, oldGoalStates))
    } yield ()
  }

  private def createMissingUserStates(oldCert: Certificate, versionId: Long): DBIO[Unit] = {
    val lpId = oldCert.id
    val startDate = if (oldCert.isActive) oldCert.activationDate else None
    val now = DateTime.now
    for {
      missingGoalStatuses <- newTables.getUsersWithoutGoalStatuses(lpId, versionId)
      _ <- {
        userGoalStatusDBIO.insert {
          missingGoalStatuses map { case (goal, userId) =>
            UserStatusUtil.getNewStatus(userId, goal, now, startDate)
          }
        }
      }

      userWithoutLPStateIds <- newTables.getUsersWithoutLPStatuses(lpId)
      _ <- {
        userLPStatusDBIO.insert {
          userWithoutLPStateIds.map { userId =>
            UserLPStatus(userId, lpId, versionId, CertificateStatuses.InProgress, now, now, 0)
          }
        }
      }
    } yield ()
  }

  private def calculateGoalGroupState(versionId: Long, groupData: (Goal, GoalGroup),
                                      userId: Long, now: DateTime): DBIO[_] = {
    val (goal, goalGroup) = groupData
    for {
      subGoals <- goalDBIO.getByVersionIdAndParentGroupId(versionId, Some(goal.id))
      subStatuses <- userGoalStatusDBIO.getByGoalGroupId(goal.id, userId)
      _ <- updateGroup(goal, goalGroup, userId, subGoals, subStatuses, now)
    } yield {}
  }

  private def updateGroup(goal: Goal, goalGroup: GoalGroup, userId: Long,
                          subGoals: Seq[Goal], subStatuses: Seq[UserGoalStatus], now: DateTime): DBIO[_] = {
    val (requiredCount, completedCount) = GroupsGoalChecker
      .getGroupGoalsCounts(goalGroup, subGoals, subStatuses)

    val complete = GroupsGoalChecker.isComplete(goalGroup, subGoals, subStatuses)

    val endDate = goal.timeLimit.map(now plus _)
    val status = if (complete) GoalStatuses.Success else GoalStatuses.InProgress

    userGoalStatusDBIO.insert(
      UserGoalStatus(userId, goal.id, status, now, now, requiredCount, completedCount, endDate)
    )
  }

  private def setAutoIncForLPTable(nextId: Long) = {
    implicit val dialect = Dialect(profile.asInstanceOf[JdbcDriver])
      .getOrElse(throw new Exception(s"There is no dialect for profile $profile"))

    val lpMigration = TableMigration(newTables.learningPathTQ)
    val versionMigration = TableMigration(newTables.versionTQ)
    val userLPStatusMigration = TableMigration(newTables.userLPStatusTQ)
    val memberMigration = TableMigration(newTables.membersTQ)
    val membershipMigration = TableMigration(newTables.usersMembershipTQ)

    MigrationSeq(
      versionMigration.dropForeignKeys(_.learningPath),
      userLPStatusMigration.dropForeignKeys(_.learningPath),
      memberMigration.dropForeignKeys(_.learningPath),
      membershipMigration.dropForeignKeys(_.learningPath),

      lpMigration.setAutoInc(_.id, nextId),

      versionMigration.addForeignKeys(_.learningPath),
      userLPStatusMigration.addForeignKeys(_.learningPath),
      memberMigration.addForeignKeys(_.learningPath),
      membershipMigration.addForeignKeys(_.learningPath)
    ).action
  }


  private lazy val newTables = new {
    // profile should be defined before constructor, MemberTableComponent uses it
    override val profile: JdbcProfile = DataMigrator.this.profile
  } with LearningPathTables with SlickProfile
    with LPMemberTableComponent
    with MemberDBActions {

    lazy val learningPathWithoutAutoIncTQ: TableQuery[LearningPathTableWithoutAutoInc] = TableQuery[LearningPathTableWithoutAutoInc]

    override implicit def executionContext: ExecutionContext = self.executionContext

    private lazy val selectLPNotSuccessStatusesWithoutProgressQ = Compiled { versionId: Rep[Long] =>
      userLPStatusTQ filter { status =>
        status.versionId === versionId && status.status =!= CertificateStatuses.Success && status.progress < 0.001
      }
    }

    private lazy val selectCurrentVersionIdByLpIdQ = Compiled { id: Rep[Long] =>
      learningPathWithoutAutoIncTQ
        .filter(_.id === id)
        .map(_.currentVersionId)
    }

    private val getUsersWithoutGoalStatusesQ = Compiled { (lpId: Rep[Long], versionId: Rep[Long]) =>
      goalTQ join usersMembershipTQ filter {
        case (goal, member) => goal.versionId === versionId && member.entityId === lpId
      } joinLeft userGoalStatusTQ on {
        case ((goal, member), status) => status.goalId === goal.id && status.userId === member.userId
      } filter {
        case ((goal, _), status) => status.map(_.status).isEmpty && goal.goalType =!= GoalTypes.Group
      } map {
        case ((goal, member), _) => (goal, member.userId)
      }
    }


    private val getUsersWithoutLPStatusesQ =
      Compiled { lpId: Rep[Long] =>
        usersMembershipTQ joinLeft userLPStatusTQ on {
          case (user, status) => status.userId === user.userId && status.learningPathId === lpId
        } filter {
          case (user, status) => user.entityId === lpId && status.map(_.status).isEmpty
        } map {
          case (user, _) => user.userId
        }
      }

    def insertLearningPathWithoutAutoInc(learningPath: LearningPath): DBIO[_] = {
      learningPathWithoutAutoIncTQ += learningPath
    }

    def setCurrentVersion(lpId: Long, versionId: Long): DBIO[_] = {
      selectCurrentVersionIdByLpIdQ(lpId) update Some(versionId)
    }

    def addMembers(members: Seq[Member]): DBIO[_] = {
      addMembersAction(members) andThen
        addUserMembershipsAction(members flatMap { member =>
          liferay.getMemberUserIds(member) map { userId =>
            UserMembership(userId, member.id, member.tpe, member.entityId)
          }
        })
    }

    def getUsersWithoutGoalStatuses(lpId: Long, versionId: Long): DBIO[Seq[(Goal, Long)]] = {
      getUsersWithoutGoalStatusesQ(lpId, versionId).result.map(_.distinct)
    }

    def getUsersWithoutLPStatuses(lpId: Long): DBIO[Seq[Long]] = {
      getUsersWithoutLPStatusesQ(lpId).result.map(_.distinct)
    }

    def selectLPNotSuccessStatusesWithoutProgress(versionId: Long): DBIO[Seq[UserLPStatus]] = {
      selectLPNotSuccessStatusesWithoutProgressQ(versionId).result
    }
  }

  private implicit class DBActionExtensions[T](val action: DBIO[T]) {

    //TODO remove after debug?
    def withErrorLog(message: String): DBIO[T] = {
      action cleanUp {
        case Some(ex) =>
          log.error(message + s"; sql: ${action.getDumpInfo}", ex)
          DBIO.failed(ex)
        case _ => DBIO.successful("")
      }
    }

  }

}