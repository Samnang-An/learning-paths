package com.arcusys.valamis.learningpath.migration

import com.arcusys.valamis.learningpath.services.impl.tables.competences.CompetencesTableManager
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickDBInfo
import org.apache.commons.logging.Log

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

class CompetencesMigration(dbInfo: SlickDBInfo,
                           log: Log)
                          (implicit val executionContext: ExecutionContext) {

  import dbInfo.profile.api._

  def run(): Unit = {
    val tableManager = new CompetencesTableManager(dbInfo.db, dbInfo.profile)

    log.info("Checking LP-Competences relation tables existence...")

    val action = tableManager.hasTables flatMap {
      case true =>
        log.info("LP-Competences relation tables exists already.")
        DBIO.successful{}
      case false =>
        log.info("No LP-Competences relation tables. Creating it...")
        tableManager.createTables().map { _ =>
          log.info("Done.")
        }
    }

    Await.result(
      dbInfo.db.run(action.transactionally),
      Duration.Inf
    )
  }
}
