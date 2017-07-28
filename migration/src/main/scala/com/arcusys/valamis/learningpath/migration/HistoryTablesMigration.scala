package com.arcusys.valamis.learningpath.migration

import com.arcusys.valamis.learningpath.services.impl.tables.history.HistoryTableManager
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickDBInfo
import org.apache.commons.logging.Log

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

/**
  * Created by mminin on 05/04/2017.
  */
class HistoryTablesMigration(dbInfo: SlickDBInfo,
                             log: Log)
                            (implicit val executionContext: ExecutionContext) {

  import dbInfo.profile.api._

  def run(): Unit = {
    val tableManager = new HistoryTableManager(dbInfo.db, dbInfo.profile)

    log.info("Running history tables migration...")

    val action = tableManager.hasTables() flatMap {
      case true =>
        log.info("History tables are found")
        DBIO.successful{}
      case false =>
        log.info("No history tables are found.")
        tableManager.createTables()
    }

    Await.result(
      dbInfo.db.run(action.transactionally),
      Duration.Inf
    )
  }
}
