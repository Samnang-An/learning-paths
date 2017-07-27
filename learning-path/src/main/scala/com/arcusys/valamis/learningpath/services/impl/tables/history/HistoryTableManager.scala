package com.arcusys.valamis.learningpath.services.impl.tables.history

import com.arcusys.valamis.learningpath.services.impl.tables.utils.{SlickProfile, TableManagerBase}
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

import scala.concurrent.ExecutionContext
import scala.language.reflectiveCalls


class HistoryTableManager(db: JdbcBackend#DatabaseDef,
                          val profile: JdbcProfile)
                         (implicit val executionContext: ExecutionContext)
  extends TableManagerBase
    with SlickProfile
    with HistoryTableComponent {

  import profile.api._

  def createTables(): DBIO[Unit] = {
    (userStatusHistoryTQ.schema ++ lpHistoryTQ.schema).create.transactionally
  }


  def hasTables(): slick.dbio.DBIO[Boolean] = {
    hasTable(lpHistoryTQ.baseTableRow.tableName)
  }
}
