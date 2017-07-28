package com.arcusys.valamis.learningpath.services.impl.tables.competences

import com.arcusys.valamis.learningpath.services.impl.tables.LearningPathTables
import com.arcusys.valamis.learningpath.services.impl.tables.utils.{SlickProfile, TableManagerBase}
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

import scala.concurrent.ExecutionContext
import scala.language.reflectiveCalls


class CompetencesTableManager(db: JdbcBackend#DatabaseDef,
                              val profile: JdbcProfile)
                             (implicit val executionContext: ExecutionContext)
  extends TableManagerBase
    with SlickProfile
    with LearningPathTables {

  import profile.api._

  def createTables(): DBIO[Unit] = {
    (recommendedCompetencesTQ.schema ++ improvingCompetencesTQ.schema).create.transactionally
  }

  def hasTables: slick.dbio.DBIO[Boolean] = {
    hasTable(recommendedCompetencesTQ.baseTableRow.tableName)
  }
}
