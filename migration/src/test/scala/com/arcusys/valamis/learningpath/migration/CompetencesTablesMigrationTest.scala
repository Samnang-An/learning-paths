package com.arcusys.valamis.learningpath.migration

import com.arcusys.valamis.learningpath.migration.impl.LoggerTestImpl
import com.arcusys.valamis.learningpath.models.{Competence, LPVersion}
import com.arcusys.valamis.learningpath.services.CompetenceService
import com.arcusys.valamis.learningpath.services.impl.CompetenceServiceImpl
import com.arcusys.valamis.learningpath.services.impl.tables.LearningPathTables
import com.arcusys.valamis.learningpath.services.impl.tables.utils.{SlickDBInfo, SlickProfile}
import com.arcusys.valamis.learningpath.utils.DbActions
import com.arcusys.valamis.slick.util.SlickDbTestBase
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by pkornilov on 6/14/17.
  */
class CompetencesTablesMigrationTest extends FunSuite
  with BeforeAndAfter
  with SlickDbTestBase
  with SlickProfile
  with LearningPathTables
  with Matchers {
  self =>

  private lazy val dbActions: DbActions = new DbActions(dbInfo.db, dbInfo.profile)

  private lazy val log = new LoggerTestImpl

  private lazy val recommendedCompetenceService = new CompetenceServiceImpl(
    dbActions,
    dbActions.recommendedCompetenceDBIOActions
  )

  private lazy val improvingCompetenceService = new CompetenceServiceImpl(
    dbActions,
    dbActions.improvingCompetenceDBIOActions
  )

  lazy val dbInfo = new SlickDBInfo {
    val profile: JdbcProfile = self.profile
    val db: JdbcBackend#DatabaseDef = self.db
  }

  private implicit val testCompanyId = 123L
  private val testUserId = 456L

  import profile.api._

  before {
    initDb()
    await {
      db.run {
        (learningPathTQ.schema ++ versionTQ.schema).create
      }
    }
  }

  after {
    closeDb()
  }

  test("should create tables if they don't exist") {
    new CompetencesMigration(dbInfo, log).run()
    checkTables(recommendedCompetenceService)
    checkTables(improvingCompetenceService)
  }

  test("should not attempt to create tables twice") {
    new CompetencesMigration(dbInfo, log).run()
    new CompetencesMigration(dbInfo, log).run()
    checkTables(recommendedCompetenceService)
    checkTables(improvingCompetenceService)
  }

  private def checkTables(service: CompetenceService): Unit = {
    val testCompetence = Competence(
      skillId = 1L,
      skillName = "Skill 1",
      levelId = 2L,
      levelName = "Level 2"
    )

    await {
      for {
        testLpId <- createTestLearningPath()
        _ <- service.create(testLpId, testCompetence)
        res <- service.getCompetencesForLPLastDraft(testLpId)
      } yield res
    } shouldBe Seq(testCompetence)
  }

  private def createTestLearningPath(): Future[Long] = db.run {
    for {
      lpId <- dbActions.learningPathDBIO.insert(testCompanyId, testUserId, active = false, hasDraft = true)
      _ <- dbActions.versionDBIO.insert(LPVersion(
        learningPathId = lpId,
        title = "test lp1",
        description = None,
        logo = None,
        courseId = None,
        validPeriod = None,
        expiringPeriod = None,
        openBadgesEnabled = false,
        openBadgesDescription = None,
        published = false,
        createdDate = DateTime.now,
        modifiedDate = DateTime.now))
    } yield lpId
  }


}
