package com.arcusys.valamis.learningpath.migration

import com.arcusys.valamis.learningpath.migration.impl._
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}
import com.arcusys.valamis.learningpath.migration.schema.old.{FileTableComponent, OldCurriculumTables}
import com.arcusys.valamis.learningpath.migration.schema.old.model.goal.{GoalStatuses => OldGoalStatuses, _}
import com.arcusys.valamis.learningpath.migration.schema.old.model.{Certificate, CertificateMember, FileRecord, PeriodTypes, MemberTypes => OldMemberTypes}
import com.arcusys.valamis.learningpath.models._
import com.arcusys.valamis.learningpath.services.exceptions.NoLearningPathError
import com.arcusys.valamis.learningpath.services.impl.tables.LearningPathTables
import com.arcusys.valamis.learningpath.services.impl.tables.utils.{SlickDBInfo, SlickProfile}
import com.arcusys.valamis.learningpath.utils.DbActions
import com.arcusys.valamis.members.picker.model.MemberTypes
import com.arcusys.valamis.members.picker.model.impl.ForcedUserInfo
import com.arcusys.valamis.slick.util.SlickDbTestBase
import org.joda.time.{DateTime, DateTimeComparator, DateTimeFieldType, Period}
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by pkornilov on 3/28/17.
  */
class CurriculumMigrationTest extends FunSuite
  with BeforeAndAfter
  with SlickDbTestBase
  with OldCurriculumTables
  with FileTableComponent
  with SlickProfile
  with TestHelpers
  with Matchers {
  self =>

  lazy val dbActions: DbActions = new DbActions(dbInfo.db, dbInfo.profile)

  lazy val dbInfo = new SlickDBInfo {
    val profile: JdbcProfile = self.profile
    val db: JdbcBackend#DatabaseDef = self.db
  }

  private val companyId1 = 20116L
  private val companyId2 = 20117L
  private val companyId3 = 20118L

  private val companyId1DefaultUserId = 20110L
  private val companyId2DefaultUserId = 20111L
  private val companyId3DefaultUserId = 20112L

  private val user1Id = 20121L
  private val user2Id = 20131L

  before {
    initDb()
  }

  after {
    closeDb()
  }

  import profile.api._

  //TODO add more tests

  test("should migrate old curriculum data to new LP tables") {
    val now = getNowWithoutMillis()

    createOldTables()
    implicit val companyId = 20116L
    val notActiveCert = Certificate(
      id = 11,
      title = "Certificate 11",
      logo = "",
      isPermanent = false,
      description = "Description of certificate 11",
      isPublishBadge = true,
      shortDescription = "Badge description 11",
      companyId = companyId,
      validPeriodType = PeriodTypes.WEEKS,
      validPeriod = 7,
      createdAt = now,
      activationDate = None,
      isActive = false,
      scope = None
    )

    val courseId = 123L
    val assignmentId = 345L
    val eventId = 222L
    val packageId = 333L

    val goals = Seq(
      (CertificateGoal(id = -1, certificateId = -1,
        goalType = GoalType.Course,
        periodType = PeriodTypes.DAYS, periodValue = 3,
        arrangementIndex = 3,
        groupId = None, oldGroupId = None,
        modifiedDate = now, userId = None
      ), Some(CourseGoal(goalId = -1, certificateId = -1, courseId))),

      (CertificateGoal(id = -1, certificateId = -1,
        goalType = GoalType.Activity,
        periodType = PeriodTypes.MONTH, periodValue = 1,
        arrangementIndex = 3,
        isOptional = true,
        groupId = None, oldGroupId = None,
        modifiedDate = now, userId = None
      ), Some(ActivityGoal(-1, -1, activityName = "Blog", count = 3))),

      (CertificateGoal(id = -1, certificateId = -1,
        goalType = GoalType.Assignment,
        periodType = PeriodTypes.WEEKS, periodValue = 2,
        arrangementIndex = 3,
        isOptional = true,
        groupId = None, oldGroupId = None,
        modifiedDate = now, userId = None
      ), Some(AssignmentGoal(goalId = -1, certificateId = -1, assignmentId))),

      (CertificateGoal(id = -1, certificateId = -1,
        goalType = GoalType.Statement,
        periodType = PeriodTypes.YEAR, periodValue = 5,
        arrangementIndex = 3,
        groupId = None, oldGroupId = None,
        modifiedDate = now, userId = None
      ), Some(StatementGoal(-1, -1, verb = "Completed", obj = "ulitka"))),

      (CertificateGoal(id = -1, certificateId = -1,
        goalType = GoalType.TrainingEvent,
        periodType = PeriodTypes.UNLIMITED, periodValue = 3,
        arrangementIndex = 3,
        groupId = None, oldGroupId = None,
        modifiedDate = now, userId = None
      ), Some(TrainingEventGoal(-1, -1, eventId))),

      (CertificateGoal(id = -1, certificateId = -1,
        goalType = GoalType.Package,
        periodType = PeriodTypes.UNLIMITED, periodValue = -1,
        arrangementIndex = 3,
        groupId = None, oldGroupId = None,
        modifiedDate = now, userId = None
      ), Some(PackageGoal(-1, -1, packageId)))
    )

    val deletedGoals = goals.take(3) map { case (g, d) => (g.copy(isDeleted = true), d) }

    execSync {
      for {
        _ <- createCertificate(notActiveCert)
        _ <- addGoals(notActiveCert.id, goals ++ deletedGoals)
        _ <- addUser(notActiveCert.id, user1Id)
      //TODO add creating of other data - members, states, etc.
      } yield ()

    }

    runMigration()


    //check creation of missing statuses
    await(db.run(
      for {
        lpStatusCount <- dbActions.userLPStatusDBIO.getByLearningPathId(notActiveCert.id).map(_.size)
        goalStatusCount <- dbActions.userGoalStatusDBIO.getByLearningPathAndUser(notActiveCert.id, user1Id).map(_.size)
      } yield (lpStatusCount, goalStatusCount))) should be(1, 6) //TODO improve this check

    val (notActiveLp, versionInfo, lpGoals) = getLPWithData(notActiveCert.id)
    notActiveLp shouldBe Some(
      LearningPath(id = notActiveCert.id,
        activated = false,
        companyId = companyId,
        userId = 20110,
        hasDraft = false,
        currentVersionId = Some(1)
      ))

    val (_, version) = versionInfo.get

    version shouldBe LPVersion(
      learningPathId = notActiveCert.id,
      title = notActiveCert.title,
      description = Some(notActiveCert.description),
      logo = None,
      courseId = None,
      validPeriod = Some(Period.weeks(7)),
      expiringPeriod = Some(Period.days(30)),
      openBadgesEnabled = true,
      openBadgesDescription = Some(notActiveCert.shortDescription),
      published = true,
      createdDate = notActiveCert.createdAt,
      modifiedDate = notActiveCert.createdAt
    )
    lpGoals.sortBy(_._1.id) shouldBe Seq(
      (Goal(1, None, 1, None, GoalTypes.Course, 3, Some(Period.days(3)), false, now), GoalCourse(1, courseId)),
      (Goal(2, None, 1, None, GoalTypes.LRActivity, 3, Some(Period.months(1)), true, now), GoalLRActivity(2, "Blog", 3)),
      (Goal(3, None, 1, None, GoalTypes.Assignment, 3, Some(Period.weeks(2)), true, now), GoalAssignment(3, assignmentId)),
      (Goal(4, None, 1, None, GoalTypes.Statement, 3, Some(Period.years(5)), false, now), GoalStatement(4, "Completed", "ulitka", "")),
      (Goal(5, None, 1, None, GoalTypes.TrainingEvent, 3, None, false, now), GoalTrainingEvent(5, eventId)),
      (Goal(6, None, 1, None, GoalTypes.Lesson, 3, None, false, now), GoalLesson(6, packageId))
    )
  }

  test("should not run migration more than once") {
    createOldTables()
    runMigration(assetEntryIdMap = Map())
    runMigration(assetEntryIdMap = Map())
  }

  test("should set auto increment for learning path table after migration") {
    createOldTables()
    execSync(createSampleNotActiveCertificate(11, "Certificate 11"))

    runMigration(assetEntryIdMap = Map())

    val newId =
      execSync(dbActions.learningPathDBIO.insert(companyId1, companyId1DefaultUserId, active = true, hasDraft = true))

    assert(newId === 12)

  }

  test("should correctly handle goals without custom part") {
    createOldTables()
    val certId = 11L

    val brokenGoals = Seq(
      (sampleCertificateGoal(GoalType.Course), None),
      (sampleCertificateGoal(GoalType.Activity), None),
      (sampleCertificateGoal(GoalType.Assignment), None),
      (sampleCertificateGoal(GoalType.Package), None),
      (sampleCertificateGoal(GoalType.Statement), None),
      (sampleCertificateGoal(GoalType.TrainingEvent), None)
    )

    execSync {
      for {
        _ <- createSampleNotActiveCertificate(certId, "Certificate 11")
        _ <- addGoals(certId, brokenGoals)
      } yield ()
    }

    runMigration()

    val goals = execSync {
      for {
        version <- dbActions.versionDBIO.getCurrentByLearningPathId(certId)
        goals <- version match {
          case Some((versionId, _)) => dbActions.goalDBIO.getByVersionId(versionId)
          case None => DBIO.failed(new NoSuchElementException(s"no version for lp $certId"))
        }
      } yield goals
    }

    goals shouldBe empty

  }

  test("should correctly handle logo name") {
    val now = getNowWithoutMillis()

    createOldTables()
    implicit val companyId = 20116
    val certId = 42L
    val cert2Id = 43L

    val cert = Certificate(
      id = certId,
      title = "Certificate with logo without extension",
      logo = "logo",
      description = "Description of certificate ",
      isPublishBadge = true,
      shortDescription = "Badge description ",
      companyId = companyId,
      validPeriodType = PeriodTypes.WEEKS,
      validPeriod = 7,
      createdAt = now,
      activationDate = None,
      scope = None
    )

    val cert2 = cert.copy(id = cert2Id, logo = "logo.png")

    execSync {
      for {
        _ <- files ++= Seq(
          FileRecord("files/42/logo", Some(Array[Byte](1, 2, 3))),
          FileRecord("files/43/logo.png", Some(Array[Byte](4, 5, 6)))
        )
        _ <- createCertificate(cert)
        _ <- createCertificate(cert2)
      } yield ()
    }

    runMigration()

    execSync(dbActions.versionDBIO.getCurrentByLearningPathId(certId)).fold(throw new NoLearningPathError(certId)) {
      case (_, lpVersion) =>
        val logo = lpVersion.logo.getOrElse(throw new NoSuchElementException("no logo for cert: " + certId))
        assert(!logo.isEmpty && !logo.contains("/"))
    }

    execSync(dbActions.versionDBIO.getCurrentByLearningPathId(cert2Id)).fold(throw new NoLearningPathError(cert2Id)) {
      case (_, lpVersion) =>
        val logo = lpVersion.logo.getOrElse(throw new NoSuchElementException("no logo for cert: " + cert2Id))
        assert(!logo.isEmpty && !logo.contains("/") && logo.endsWith(".png"))
    }

  }

  test("should not run data migration if there is no old tables") {
    val userId = 20110L
    runMigration()

    val newLp = await(
      db.run(for {
        lpId <- dbActions.learningPathDBIO.insert(companyId1, userId, active = false, hasDraft = false)
        newLp <- dbActions.learningPathDBIO.getById(lpId)(companyId1)
      } yield newLp)
    )

    newLp shouldBe Some(LearningPath(1, activated = false, companyId1, userId, hasDraft = false, None))
  }

  test("should correctly calculate lp progress") {
    val now =getNowWithoutMillis

    createOldTables()
    val certId = 11L

    val package1Id = 12L
    val package2Id = 13L
    val package3Id = 14L
    val package4Id = 15L
    val package5Id = 16L

    val goals = Seq(
      (sampleCertificateGoal(GoalType.Package), Some(PackageGoal(-1, -1, package1Id))),
      (sampleCertificateGoal(GoalType.Package), Some(PackageGoal(-1, -1, package2Id))),
      (sampleCertificateGoal(GoalType.Package), Some(PackageGoal(-1, -1, package3Id))),
      (sampleCertificateGoal(GoalType.Package), Some(PackageGoal(-1, -1, package4Id))),
      (sampleCertificateGoal(GoalType.Package), Some(PackageGoal(-1, -1, package5Id)))
    )

    await(db.run(for {
      _ <- createSampleActiveCert(certId, "Title")
      goals <- addGoals(certId, goals)
      _ <- addUser(certId, user1Id)
      _ <- addUser(certId, user2Id)

      //for user1 there is no certificate state in old tables
      _ <- DBIO.seq(
        addUserGoalState(goals.head, user1Id, OldGoalStatuses.Success, now),
        addUserGoalState(goals(1), user1Id, OldGoalStatuses.InProgress, now),
        addUserGoalState(goals(2), user1Id, OldGoalStatuses.Success, now),
        addUserGoalState(goals(3), user1Id, OldGoalStatuses.Failed, now),
        addUserGoalState(goals(4), user1Id, OldGoalStatuses.Failed, now)
      )

      //for user2 there IS certificate state in old tables
      _ <- DBIO.seq(
        addUserGoalState(goals.head, user2Id, OldGoalStatuses.Success, now),
        addUserGoalState(goals(1), user2Id, OldGoalStatuses.InProgress, now),
        addUserGoalState(goals(2), user2Id, OldGoalStatuses.Success, now),
        addUserGoalState(goals(3), user2Id, OldGoalStatuses.Success, now),
        addUserGoalState(goals(4), user2Id, OldGoalStatuses.Failed, now)
      )
      _ <- addUserLPState(certId, user2Id, CertificateStatuses.InProgress, now)

    } yield ()))

    runMigration()

    val (lpStatus1, lpStatus2) = await(db.run(for {
      lpStatus1 <- dbActions.userLPStatusDBIO.getByUserAndLearningPath(certId, user1Id)
      lpStatus2 <- dbActions.userLPStatusDBIO.getByUserAndLearningPath(certId, user2Id)
    } yield (lpStatus1, lpStatus2)))

    lpStatus1.map(_.copy(startedDate = now, modifiedDate = now)) shouldBe
      Some(UserLPStatus(user1Id, certId, 1, CertificateStatuses.InProgress, now, now, 0.4))

    lpStatus2 shouldBe Some(UserLPStatus(user2Id, certId, 1, CertificateStatuses.InProgress, now, now, 0.6))
  }

  //TODO add test of calculating group goal status

  private def getLPWithData(lpId: Long)(implicit companyId: Long) = {
    execSync(
      for {
        lp <- dbActions.learningPathDBIO.getById(lpId)
        version <- dbActions.versionDBIO.getCurrentByLearningPathId(lpId)
        goalsWithData <- version match {
          case Some((versionId, _)) => DBIO.sequence(Seq(
            dbActions.goalActivityDBIO.getWithGoalInfoByVersionId(versionId),
            dbActions.goalAssignmentDBIO.getWithGoalInfoByVersionId(versionId),
            dbActions.goalCourseDBIO.getWithGoalInfoByVersionId(versionId),
            dbActions.goalStatementDBIO.getWithGoalInfoByVersionId(versionId),
            dbActions.goalTrainingEventDBIO.getWithGoalInfoByVersionId(versionId),
            dbActions.goalLessonDBIO.getWithGoalInfoByVersionId(versionId)
          )) map (_.flatten)
          case None => DBIO.failed(new NoSuchElementException(s"no version for lp $lpId"))
        }
      } yield (lp, version, goalsWithData)
    )
  }

  private def runMigration(assetEntryIdMap: Map[Long, Long] = Map()): Unit = {

    new CurriculumToLPMigration(
      dbInfo, dbActions, liferayHelper, companyService,
      new AssetEntryServiceTestImpl(assetEntryIdMap),
      logoFileStorage, log) {
      override def isMemberExisted(memberId: Long, memberType: MemberTypes.Value): Boolean =
        true //TODO isMemberExisted test data
    }.run(throwEx = true)

  }

  private def createOldTables(): Unit = {
    println("Creating old tables...")
    Await.result(
      db.run {
        (certificatesTQWithoutAutoInc.schema
          ++ certificateGoalGroups.schema
          ++ certificateGoals.schema
          ++ activityGoals.schema
          ++ assignmentGoals.schema
          ++ courseGoals.schema
          ++ packageGoals.schema
          ++ statementGoals.schema
          ++ trainingEventGoals.schema
          ++ certificateMembers.schema
          ++ certificateStates.schema
          ++ certificateGoalStates.schema
          ++ files.schema
          ).create
      },
      Duration.Inf)
  }

  private def execSync[T](action: DBIO[T]): T = {
    Await.result(db.run(action), Duration.Inf)
  }

  private lazy val companyService = new CompanyServiceTestImpl(Map(
    companyId1 -> companyId1DefaultUserId,
    companyId2 -> companyId2DefaultUserId,
    companyId3 -> companyId3DefaultUserId
  ))

  private lazy val liferayHelper = new LiferayHelperTestImpl(
    users = Seq(
      ForcedUserInfo(user1Id, "user 1", "/logo/u1", Nil, Nil, Nil, Nil),
      ForcedUserInfo(user2Id, "user 2", "/logo/u1", Nil, Nil, Nil, Nil)
    ))
  private lazy val log = new LoggerTestImpl
  private lazy val logoFileStorage = new LogoFileStorageImpl

  lazy val newTables = new {
    // profile should be defined before constructor, MemberTableComponent uses it
    val profile: JdbcProfile = self.profile
  } with LearningPathTables
    with SlickProfile {

  }
}
