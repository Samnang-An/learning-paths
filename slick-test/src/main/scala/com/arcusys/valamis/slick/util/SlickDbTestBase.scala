package com.arcusys.valamis.slick.util

import java.sql.Connection

import slick.driver.JdbcDriver
import slick.jdbc.JdbcBackend

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global


trait SlickDbTestBase {

  //to isolate parallel tests we will use different databases
  lazy val dbName: String = getClass.getName.split('.').last


  private val properties = SlickTestProperties(dbName)

  lazy val profile: JdbcDriver = SlickHelper.getSlickDriver(properties.jdbcDriver)
  lazy val rootDB: JdbcBackend#DatabaseDef = getDB(properties.mainJdbcUrl, root = true)
  lazy val db: JdbcBackend#DatabaseDef = getDB(properties.testJdbcUrl, root = false)


  var connection: Option[Connection] = None

  protected def initDb(): Unit = {
    import profile.api._

    properties.initDbSql foreach { statements =>
      await(
        rootDB run {
          DBIO.sequence(statements.split(";").toList map { q =>
            sqlu"#$q"
          })
        }
      )
    }

    if (properties.aliveConnection) {
      connection = Some(db.source.createConnection())
    }
  }

  protected def closeDb(): Unit = {
    import profile.api._

    connection.foreach(_.close)
    connection = None

    properties.dropDbSql
      .map(q => rootDB.run(sqlu"#$q"))
      .foreach(await)
  }

  private def getDB(jdbcUrl: String, root: Boolean) = {
    profile.profile.backend.Database.forURL(
      jdbcUrl,
      if (root) properties.userName else properties.testUserName,
      if (root) properties.password else properties.testPassword,
      driver = properties.jdbcDriver
    )
  }

  def await[T](f: Future[T]): T = {
    Await.result(f, Duration.Inf)
  }
}
