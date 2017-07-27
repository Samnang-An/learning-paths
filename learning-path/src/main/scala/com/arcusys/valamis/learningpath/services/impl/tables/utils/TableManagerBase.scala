package com.arcusys.valamis.learningpath.services.impl.tables.utils

import java.sql.SQLException

import com.arcusys.slick.drivers.OracleDriver

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}


trait TableManagerBase { self: SlickProfile =>

  implicit def executionContext: ExecutionContext
  import profile.api._

  protected def hasTable(tableName: String): DBIO[Boolean] = {
    profile match {
      case OracleDriver =>
        val checkAction =
          sql"""SELECT COUNT(*) FROM #$tableName WHERE 1 = 0""".as[Int].headOption.map(_ => true)

        checkAction.asTry flatMap {
          case Success(_) => DBIO.successful(true)
          case Failure(ex: SQLException) if ex.getErrorCode == 942 =>
            DBIO.successful(false)
          case Failure(ex) => DBIO.failed(ex)

        }
      case _ => profile.defaultTables
        .map(tables => tables.exists(_.name.name == tableName))
    }
  }
}
