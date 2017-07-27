package com.arcusys.valamis.learningpath.services.impl.tables

import com.arcusys.valamis.learningpath.services.impl.tables.history.HistoryTableComponent
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickProfile
import org.scalatest.FunSuite
import org.scalatest.Matchers._
import slick.driver.{H2Driver, JdbcProfile}

class UserStateHistoryTableTest extends FunSuite {

  lazy val tables = {
    new HistoryTableComponent with SlickProfile {
      override val profile: JdbcProfile = H2Driver
    }
  }

  test("table name should be like in previous release") {
    val tableName = tables.userStatusHistoryTQ.baseTableRow.tableName

    tableName should equal("LEARN_CERT_STATE_HSTRY")
  }
}
