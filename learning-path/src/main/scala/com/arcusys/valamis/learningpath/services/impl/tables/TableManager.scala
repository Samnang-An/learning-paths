package com.arcusys.valamis.learningpath.services.impl.tables

import com.arcusys.valamis.learningpath.services.impl.tables.utils.{SlickProfile, TableManagerBase}
import org.apache.commons.logging.Log
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

import scala.concurrent.ExecutionContext
import scala.language.reflectiveCalls

/**
  * Created by mminin on 26/01/2017.
  */
class TableManager(db: JdbcBackend#DatabaseDef,
                   val profile: JdbcProfile,
                   log: Option[Log])
                  (implicit val executionContext: ExecutionContext)
  extends TableManagerBase
    with LearningPathTables
    with SlickProfile {


  import profile.api._

  def hasTables(): slick.dbio.DBIO[Boolean] = {
    hasTable(learningPathTQ.baseTableRow.tableName)
  }

  private lazy val learningPathWithoutAutoIncTQ = TableQuery[LearningPathTableWithoutAutoInc]

  def createTables(withAutInc: Boolean): slick.dbio.DBIO[Unit] = {
    log.foreach(_.info("Creating LP tables..."))

    val learningPathTableQuery = if (withAutInc) {
      learningPathTQ.schema
    } else {
      //auto inc will be set after migrating old data
      learningPathWithoutAutoIncTQ.schema
    }
    (learningPathTableQuery
      ++ versionTQ.schema
      ++ goalTQ.schema
      ++ goalGroupTQ.schema
      ++ goalLessonTQ.schema
      ++ goalLRActivityTQ.schema
      ++ goalAssignmentTQ.schema
      ++ goalStatementTQ.schema
      ++ goalWebContentTQ.schema
      ++ goalCourseTQ.schema
      ++ membersTQ.schema
      ++ usersMembershipTQ.schema
      ++ userGoalStatusTQ.schema
      ++ userLPStatusTQ.schema
      ++ goalTrainingEventTQ.schema
      ).create.transactionally

  }
}
