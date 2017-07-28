package com.arcusys.valamis.learningpath.web.servlets

import com.arcusys.valamis.learningpath.services.impl.tables.TableManager
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickDBInfo
import com.arcusys.valamis.learningpath.ServletImpl
import com.arcusys.valamis.learningpath.services.impl.tables.competences.CompetencesTableManager
import com.arcusys.valamis.learningpath.services.impl.tables.history.HistoryTableManager
import com.arcusys.valamis.learningpath.web.servlets.utils.TestHelper
import com.arcusys.valamis.slick.util.SlickDbTestBase
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatra.test.scalatest.ScalatraSuite
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

import scala.concurrent.duration.Duration
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by mminin on 06/02/2017.
  */
abstract class LPServletTestBase extends FunSuite
  with BeforeAndAfter
  with ScalatraSuite
  with SlickDbTestBase
  with TestHelper { self =>

  before {
    initDb()
    createTables()
  }

  after {
    closeDb()
  }

  lazy val dbInfo = new SlickDBInfo {
    val profile: JdbcProfile = self.profile
    val db: JdbcBackend#DatabaseDef = self.db
  }

  def createTables(): Unit = await {
    for {
      _ <- db.run(new TableManager(db, profile, None).createTables(withAutInc = true))
      _ <- db.run(new HistoryTableManager(db, profile).createTables())
      _ <- db.run(new CompetencesTableManager(db, profile).createTables())
    } yield ()
  }

  def servlet = new ServletImpl(dbInfo)

  addServlet(servlet, "/*")
}
