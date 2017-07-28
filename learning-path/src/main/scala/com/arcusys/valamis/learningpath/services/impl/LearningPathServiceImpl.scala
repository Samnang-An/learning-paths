package com.arcusys.valamis.learningpath.services.impl

import com.arcusys.valamis.learningpath.models._
import com.arcusys.valamis.learningpath.services.exceptions.{AlreadyActivatedError, NoLearningPathDraftError, NoLearningPathError}
import com.arcusys.valamis.learningpath.services.impl.utils.{CopyCompetencesSupport, CopyGoalsSupport, UserStatusUtil}
import com.arcusys.valamis.learningpath.services._
import com.arcusys.valamis.learningpath.services.impl.actions.{ImprovingCompetenceDBIOActions, RecommendedCompetenceDBIOActions}
import com.arcusys.valamis.learningpath.tasks.TaskManager
import com.arcusys.valamis.learningpath.utils.{DbActions, DbActionsSupport}
import com.arcusys.valamis.members.picker.model.SkipTake
import com.arcusys.valamis.members.picker.service.MemberService
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

/**
  * Created by mminin on 26/01/2017.
  */
class LearningPathServiceImpl(val dbActions: DbActions,
                              val improvingCompetenceDBIO: ImprovingCompetenceDBIOActions,
                              val recommendedCompetenceDBIO: RecommendedCompetenceDBIOActions,
                              logoFileStorage: FileStorage,
                              memberService: MemberService,
                              taskManager: TaskManager,
                              certificateNotificationService: CertificateNotificationService,
                              modelListener: LPModelListener,
                              userLPStatusModelListener: UserLPStatusModelListener,
                              socialActivityHelper: SocialActivityHelper,
                              lpStatementService: LPStatementService)
                             (implicit val executionContext: ExecutionContext)
  extends LearningPathService
    with DbActionsSupport
    with CopyGoalsSupport
    with CopyCompetencesSupport {

  import profile.api._

  override def create(userId: Long,
                      properties: LPProperties)
                     (implicit companyId: Long): Future[(LearningPath, LPVersion)] = {
    val now = DateTime.now
    val logo = None

    val action = for {
      learningPathId <- learningPathDBIO.insert(companyId, userId, active = false, hasDraft = true)
      versionId <- versionDBIO.insert(createNewVersion(learningPathId, logo, properties, now))
      _ <- learningPathDBIO.updateCurrentVersionId(learningPathId, versionId)
      learningPath <- learningPathDBIO.getById(learningPathId)
      (_, version) <- versionDBIO.getById(versionId).map(_.get)
    } yield {
      (learningPath.get, version)
    }

    for {
      (lp, version) <- db.run(action.transactionally)
      _ <- modelListener.onCreated(lp, version)
    } yield {
      (lp, version)
    }
  }

  override def updateDraft(learningPathId: Long, newProperties: LPProperties)
                          (implicit companyId: Long): Future[(LearningPath, LPVersion)] = {
    val now = DateTime.now

    val action = for {
      learningPath <- learningPathDBIO.getById(learningPathId)
        .map(_.getOrElse(throw new NoLearningPathError(learningPathId)))
      (versionId, _) <- versionDBIO.getDraftByLearningPathId(learningPathId)
        .map(_.getOrElse(throw new NoLearningPathDraftError(learningPathId)))
      _ <- versionDBIO.update(versionId, newProperties, now)

      (_, version) <- versionDBIO.getById(versionId).map(_.get)
    } yield {
      (learningPath, version)
    }

    db.run(action.transactionally)
  }

  private def createNewVersion(learningPathId: Long,
                               logo: Option[String],
                               properties: LPProperties,
                               now: DateTime) = {
    LPVersion(
      learningPathId,
      properties.title,
      properties.description,
      logo = logo,
      properties.courseId,
      properties.validPeriod,
      properties.expiringPeriod,
      properties.openBadgesEnabled,
      properties.openBadgesDescription,
      published = false,
      createdDate = now,
      modifiedDate = now
    )
  }

  def getCountByFilter(filter: LearningPathFilter)
                      (implicit companyId: Long): Future[Int] = {
    db.run {
      learningPathDBIO.getCountByFilter(filter)
    }
  }

  def getByFilter(filter: LearningPathFilter,
                  sort: LearningPathSort.Value,
                  skip: Option[Int],
                  take: Int)
                 (implicit companyId: Long): Future[Seq[(LearningPath, LPVersion)]] = {
    db.run {
      learningPathDBIO.getByFilter(filter, sort, skip.getOrElse(0), take)
    }
  }

  def getByFilterForMember(filter: LearningPathFilter,
                           userMemberId: Long,
                           joined: Option[Boolean],
                           statusFilter: Option[CertificateStatuses.Value],
                           sort: LearningPathSort.Value,
                           skip: Option[Int],
                           take: Option[Int])
                          (implicit companyId: Long): Future[Seq[LPWithInfo]] = {
    db.run {
      learningPathDBIO.getFullInfoByFilter(filter, userMemberId, joined,
        statusFilter, sort, skip.getOrElse(0), take)
    }
  }

  def getByIdForMember(learningPathId: Long,
                       userMemberId: Long)
                      (implicit companyId: Long): Future[Option[LPWithInfo]] = {
    db.run {
      learningPathDBIO.getFullInfoById(learningPathId, userMemberId)
    }
  }

  def getCountByFilterForMember(filter: LearningPathFilter,
                                userMemberId: Long,
                                joined: Option[Boolean],
                                statusFilter: Option[CertificateStatuses.Value])
                               (implicit companyId: Long): Future[Int] = {
    db.run {
      learningPathDBIO.getCountByFilterForMember(filter, userMemberId, joined, statusFilter)
    }
  }

  override def getById(id: Long)
                      (implicit companyId: Long): Future[Option[(LearningPath, LPVersion)]] = {
    val action = learningPathDBIO.getById(id).flatMap {
      case None => DBIO.successful(None)
      case Some(lp) =>
        versionDBIO.getById(lp.currentVersionId.get).map(v => Some((lp, v.get._2)))
    }

    db.run(action)
  }

  override def getByIds(ids: Seq[Long]): Future[Seq[(LearningPath, LPVersion)]] =
    db.run(versionDBIO.getWithLearningPathByIds(ids))

  override def getWithUserStatusByIds(userId: Long,
                                      ids: Seq[Long]): Future[Seq[LPWithVersionAndStatus]] = db.run {
    for {
      learningPaths <- versionDBIO.getWithLearningPathByIds(ids)
      res <- DBIO.sequence(learningPaths map { case (lp, version) =>
        userLPStatusDBIO.getByUserAndLearningPath(lp.id, userId) map { status =>
          (lp, version, status)
        }
      })
    } yield res
  }


  override def getByTitleWithSucceededUserIds(title: String,
                                              count: Option[Int] = None)
                                             (implicit companyId: Long): Future[Seq[LPWithVersionAndSucceededUserIds]] =
    db.run {
      learningPathDBIO.getByTitleWithSucceededUserIds(title, count)
    }

  override def getByUserIdsWithSucceededUserCount(userIds: Seq[Long], skipTake: Option[SkipTake] = None)
                                                 (implicit companyId: Long): Future[Seq[LPWithVersionAndSucceededUserCount]] = {
    db.run {
      learningPathDBIO.getByUserIdsWithSucceededUserIds(userIds, skipTake)
    }
  }

  override def getDraftById(id: Long)
                           (implicit companyId: Long): Future[Option[(LearningPath, LPVersion)]] = {
    val action = learningPathDBIO.getById(id).flatMap {
      case None => throw new NoLearningPathError(id)
      case Some(lp) =>
        versionDBIO.getDraftByLearningPathId(lp.id).map { version =>
          version.flatMap(v => Some((lp, v._2)))
        }
    }

    db.run(action)
  }

  def delete(id: Long)
            (implicit companyId: Long): Future[Unit] = {

    val action = for {
      lp <- learningPathDBIO.getById(id).map {
        _.getOrElse(throw new NoLearningPathError(id))
      }
      versions <- versionDBIO.getByLearningPathId(id)
      _ <- userMemberDBIO.deleteByLearningPathId(id)
      _ <- memberDBIO.deleteByLearningPathId(id)
      _ <- goalDBIO.deleteByLearningPathId(id)
      _ <- improvingCompetenceDBIO.deleteByLearningPathId(id)
      _ <- recommendedCompetenceDBIO.deleteByLearningPathId(id)
      userStatuses <- userLPStatusDBIO.getByLearningPathId(id)
      _ <- userLPStatusDBIO.deleteByLearningPath(id)
      _ <- versionDBIO.deleteByLearningPathId(id)
      _ <- learningPathDBIO.deleteById(id)
    } yield {
      (lp, versions, userStatuses)
    }

    db.run(action.transactionally)
      .flatMap { case (lp, versions, userStatuses) =>
        val lastVersion = versions.map(_._2).maxBy(_.modifiedDate.toDate)
        val logoFiles = versions.flatMap(_._2.logo).distinct

        val deleteFilesF = deleteUnusedLogoFiles(logoFiles)
        val deleteActionsF = modelListener.onDeleted(lp, lastVersion)
        val deleteStatusActionsF = userLPStatusModelListener.onDeleted(userStatuses)

        deleteFilesF
          .flatMap(_ => deleteActionsF)
          .flatMap(_ => deleteStatusActionsF)
      }
  }

  private def deleteUnusedLogoFiles(logoFiles: Seq[String])
                                   (implicit companyId: Long): Future[Unit] = {
    val filesToDeleteF = db.run(DBIO.sequence(
      logoFiles.map(logo =>
        versionDBIO.getCountByLogo(logo) map {
          case v if v == 0 => Some(logo)
          case _ => None
        }
      )
    ))

    filesToDeleteF.flatMap { files =>
      logoFileStorage.delete(files.flatten)
    }
  }

  override def publishDraft(id: Long)
                           (implicit companyId: Long): Future[Unit] = {
    val action = for {
      _ <- learningPathDBIO.getById(id).map {
        _.getOrElse(throw new NoLearningPathError(id))
      }

      (versionId, published) <- versionDBIO.getCurrentByLearningPathId(id)
        .map {
          _.getOrElse(throw new NoLearningPathError(id))
        }.map { case (versionId, version) => (versionId, version.published) }
      _ <- {
        if (published) publishNewDraft(id, versionId)
        else publishFirstTime(id, versionId)
      }

      publishedLP <- learningPathDBIO.getById(id).map(_.get)
      (_, publishedVersion) <- versionDBIO.getCurrentByLearningPathId(id).map(_.get)
      userLPStatuses <- userLPStatusDBIO.getByLearningPathId(id)
    } yield {
      val firstPublish = !published
      (publishedLP, publishedVersion, firstPublish, userLPStatuses)
    }

    db.run(action.transactionally)
      .flatMap { case (lp, version, firstPublish, userLPStatuses) =>
        taskManager.planUndefinedStatusChecker(lp.id)

        val lpActionsF = modelListener.onChanged(lp, version)
        val statusActionsF = if (firstPublish) {
          userLPStatusModelListener.onCreated(userLPStatuses)
          socialActivityHelper.addWithSet(companyId,
            lp.userId,
            version.courseId,
            classPK = Some(lp.id),
            activityType = Some(CertificateActivitesType.Published.id),
            createDate = DateTime.now)

          val userIds = userLPStatuses.map(_.userId)
          lpStatementService.sendStatementAddedUser(userIds, companyId, version)
          certificateNotificationService.sendUsersAddedNotification(lp.id, userIds)

        } else {
          Future.successful {}
        }

        lpActionsF flatMap (_ => statusActionsF)
      }
  }

  private def publishFirstTime(learningPathId: Long,
                               versionId: Long): DBIO[Unit] = {
    val now = DateTime.now()
    val lpStatus = CertificateStatuses.InProgress
    val userProgress = 0 //progress will be updated after check goals
    for {
      _ <- versionDBIO.updatePublished(versionId, published = true)
      _ <- learningPathDBIO.updateActiveCurrentVersionAndHasDraft(learningPathId, active = true, versionId, hasDraft = false)

      goals <- goalDBIO.getByVersionId(versionId)
      userMembers <- userMemberDBIO.getUserIdsByLearningPathId(learningPathId)

      _ <- userGoalStatusDBIO.insert {
        UserStatusUtil.getNewStatuses(goals, userMembers, now)
      }
      _ <- userLPStatusDBIO.insert(userMembers.map { userId =>
        UserLPStatus(userId, learningPathId, versionId, lpStatus, now, now, userProgress)
      })
    } yield {}
  }

  private def publishNewDraft(learningPathId: Long, oldVersionId: Long): DBIO[Unit] = {
    val now = DateTime.now()

    for {
      (versionId, _) <- versionDBIO.getDraftByLearningPathId(learningPathId).map {
        _.getOrElse {
          throw new NoLearningPathDraftError(learningPathId)
        }
      }

      oldStatuses <- userGoalStatusDBIO.getByVersion(oldVersionId)
      newGoals <- goalDBIO.getByVersionId(versionId)
      oldLPStatuses <- userLPStatusDBIO.getByLearningPathId(learningPathId)

      _ <- versionDBIO.updatePublished(versionId, published = true)
      _ <- learningPathDBIO.updateActiveCurrentVersionAndHasDraft(
        learningPathId, active = true, versionId, hasDraft = false
      )

      _ <- migrateUserStatuses(
        learningPathId, versionId, oldVersionId, newGoals, oldLPStatuses, oldStatuses, now
      )
    } yield {}
  }

  /**
    * find users with not Success LP status
    * and migrate to new version
    */
  private def migrateUserStatuses(lpId: Long,
                                  newVersionId: Long,
                                  oldVersionId: Long,
                                  newGoals: Seq[Goal],
                                  oldLPStatuses: Seq[UserLPStatus],
                                  oldStatuses: Seq[UserGoalStatus],
                                  now: DateTime
                                 ): DBIO[Unit] = {
    val userIds = oldLPStatuses
      .filterNot(_.status == CertificateStatuses.Success)
      .map(_.userId)

    val newGoalIdToOldGoalId = newGoals
      .map(newGoal => (newGoal.id, newGoal.oldGoalId))
      .collect { case (newId, Some(oldId)) => (newId, oldId) }
      .toMap

    DBIO.seq(userIds.map { userId =>
      val updateLpStatus = userLPStatusDBIO.updateVersionIdByLpIdAndUserId(lpId, userId, newVersionId)
      val deleteOldGoalsStatus = userGoalStatusDBIO.deleteByVersionAndUser(oldVersionId, userId)

      val insertNewGoalsStatus = userGoalStatusDBIO.insert(
        newGoals.map { newGoal =>
          newGoalIdToOldGoalId.get(newGoal.id)
            .flatMap { id => oldStatuses.find(s => s.goalId == id && s.userId == userId) }
            .map { oldGoalStatus =>
              val status = if (oldGoalStatus.status == GoalStatuses.Success) GoalStatuses.Success
              else UserStatusUtil.getStartStatus(newGoal)

              oldGoalStatus.copy(
                goalId = newGoal.id,
                status = status,
                modifiedDate = now,
                endDate = newGoal.timeLimit map { limit =>
                  oldGoalStatus.startedDate.withPeriodAdded(limit, 1)
                }
              )
            }
            .getOrElse(UserStatusUtil.getNewStatus(userId, newGoal, now))
        }
      )

      updateLpStatus andThen deleteOldGoalsStatus andThen insertNewGoalsStatus
    }: _*
    )
  }

  def deactivate(id: Long)
                (implicit companyId: Long): Future[Unit] = {
    val action = for {
      learningPath <- learningPathDBIO.getById(id) map {
        _.getOrElse(throw new NoLearningPathError(id))
      }
      (_, version) <- versionDBIO.getById(learningPath.currentVersionId.get).map(_.get)
      _ <- learningPathDBIO.updateActivated(id, activated = false)
      userIds <- userMemberDBIO.getUserIdsByLearningPathId(id)
    } yield {
      (learningPath.copy(activated = false), version, userIds)
    }

    db.run(action.transactionally)
      .flatMap { case (learningPath, version, usersIds) =>

        modelListener.onChanged(learningPath, version).flatMap(_ =>
          certificateNotificationService.sendCertificateDeactivated(learningPath.id, usersIds)
        )
      }

  }

  /**
    * reset activated to true
    * reset users progress by current version:
    * goal 'start date' to 'now' for not success goals
    * goal 'failed' statuses to 'in progress'
    */
  def activate(id: Long)
              (implicit companyId: Long): Future[Unit] = {

    val action = for {
      learningPath <- learningPathDBIO.getById(id) map {
        case Some(lp) if lp.activated => throw new AlreadyActivatedError(id)
        case None => throw new NoLearningPathError(id)
        case Some(lp) => lp
      }
      (versionId, version) <- versionDBIO.getById(learningPath.currentVersionId.get).map(_.get)
      _ <- learningPathDBIO.updateActivated(id, activated = true)
      changedStatuses <- resetGoalStatusesOnActivate(versionId, version)
    } yield {
      (learningPath.copy(activated = true), version, changedStatuses)
    }

    db.run(action.transactionally)
      .andThen { case Success(_) => taskManager.planUndefinedStatusChecker(id) }
      .flatMap { case (learningPath, version, changedStatuses) =>

        modelListener.onChanged(learningPath, version).flatMap { _ =>
          userLPStatusModelListener.onChanged(changedStatuses).flatMap { _ =>
            val usersIds = changedStatuses.map(_.userId)
            lpStatementService.sendStatementAddedUser(usersIds, companyId, version)
            certificateNotificationService.sendUsersAddedNotification(learningPath.id, usersIds)
          }
        }
      }
  }

  private def resetGoalStatusesOnActivate(versionId: Long,
                                          version: LPVersion): DBIO[Seq[UserLPStatus]] = {
    val now = DateTime.now

    if (!version.published) {
      DBIO.successful(Nil)
    } else {
      for {
        _ <- userLPStatusDBIO.updateNotCompletedByVersion(
          versionId,
          CertificateStatuses.InProgress,
          startedDate = now,
          modifiedDate = now
        )
        changedStatuses <- userLPStatusDBIO.getByVersionAndStatus(
          versionId,
          CertificateStatuses.InProgress
        )
        _ <- userGoalStatusDBIO.updateNotCompletedWithoutLimitByVersion(
          versionId,
          GoalStatuses.Undefined,
          startedDate = now,
          modifiedDate = now
        )
        //goal with limits should be updated individually due to different end dates
        goalWithLimits <- userGoalStatusDBIO.getNotSuccessWithLimitByVersion(versionId)
        _ <- DBIO.seq(goalWithLimits collect {
          case (goalId, Some(timeLimit)) =>
            userGoalStatusDBIO.updateDatesByGoal(goalId,
              GoalStatuses.Undefined,
              startedDate = now,
              endDate = Some(now.withPeriodAdded(timeLimit, 1)),
              modifiedDate = now)
        }: _*)
      } yield {
        changedStatuses
      }
    }
  }

  /**
    * copy LP with current version and goals to new LP draft
    * do not copy previous versions and next draft version
    * do not copy members
    */
  def clone(id: Long)
           (implicit companyId: Long): Future[(LearningPath, LPVersion)] = {
    val now = DateTime.now

    val action = for {
      (learningPathId, version) <- learningPathDBIO.getWithCurrentVersionById(id)
        .map(_.getOrElse(throw new NoLearningPathError(id)))

      newLearningPathId <- learningPathDBIO
        .insert(companyId, learningPathId.userId, active = false, hasDraft = true)
      newVersionId <- versionDBIO.insert(version.copy(
        learningPathId = newLearningPathId,
        published = false,
        createdDate = now,
        modifiedDate = now
      ))
      _ <- learningPathDBIO.updateCurrentVersionId(newLearningPathId, newVersionId)

      (newLearningPath, newVersion) <- learningPathDBIO
        .getWithCurrentVersionById(newLearningPathId).map(_.get)

      _ <- copyGoals(learningPathId.currentVersionId.get, newLearningPath.currentVersionId.get, now)
      _ <- copyCompetences(learningPathId.currentVersionId.get, newLearningPath.currentVersionId.get)
    } yield {
      (newLearningPath, newVersion)
    }

    db.run(action.transactionally)
  }

  protected def createNewGoalVersion(newVersionId: Long,
                                     newGroupId: Option[Long],
                                     oldGoal: Goal,
                                     now: DateTime): DBIO[Long] = {
    goalDBIO.insert(
      oldGoalId = None,
      versionId = newVersionId,
      groupId = newGroupId,
      goalType = oldGoal.goalType,
      indexNumber = oldGoal.indexNumber,
      timeLimit = oldGoal.timeLimit,
      optional = oldGoal.optional,
      now = now
    )
  }
}
