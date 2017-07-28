package com.arcusys.valamis.learningpath.services.impl.tables.utils

import java.sql.SQLException
import javax.sql.DataSource

import slick.driver.{JdbcDriver, JdbcProfile}
import slick.jdbc.JdbcBackend

trait SlickDBInfo {
  val profile: JdbcProfile

  def db: JdbcBackend#DatabaseDef
}

//TODO: move to valamis common slick module (valamis-core/valamis-slick-support)
//TODO current valamis-slick-support incompatible with slick 3.1.1
object SlickDBInfo {

  def fromDataSource(dataSource: DataSource): SlickDBInfo = {
    val dbInfo = getDbInfo(dataSource)

    val driver = (getSlickDriver _).tupled(dbInfo)

    new SlickDBInfo {
      val profile: JdbcProfile = driver.profile
      val db: JdbcBackend#DatabaseDef = profile.backend.Database.forDataSource(dataSource)
    }
  }

  private def getDbInfo(dataSource: DataSource) = {
    val connection = dataSource.getConnection

    try {
      val metaData = connection.getMetaData

      (metaData.getDatabaseProductName, metaData.getDatabaseMajorVersion)
    } finally {
      try {
        connection.close()
      } catch {
        case ex: SQLException => ex.printStackTrace()
      }
    }
  }

  private def getSlickDriver(dbName: String, dbMajorVersion: Int): JdbcDriver = {
    if (dbName.startsWith("HSQL")) {
      slick.driver.HsqldbDriver
    } else if (dbName.equals("H2")) {
      slick.driver.H2Driver
    } else if (dbName.equals("MySQL")) {
      slick.driver.MySQLDriver
    } else if (dbName.equals("PostgreSQL")) {
      slick.driver.PostgresDriver
    } else if (dbName.equals("Apache Derby")) {
      slick.driver.DerbyDriver
    } else if (dbName.startsWith("Microsoft") && (dbMajorVersion >= 9)) {
      // "Microsoft SQL Server" "Microsoft SQL Server Database"
      com.arcusys.slick.drivers.SQLServerDriver
    } else if (dbName.startsWith("Oracle") && (dbMajorVersion >= 10)) {
      com.arcusys.slick.drivers.OracleDriver
    } else if (dbName.startsWith("DB2") && (dbMajorVersion >= 9)) {
      // "DB2/NT" "DB2/LINUX"  "DB2/6000" "DB2/HPUX" "DB2/SUN" "DB2/LINUX390" "DB2/AIX64"
      com.arcusys.slick.drivers.DB2Driver
    } else {
      throw new scala.RuntimeException("Unsupported database: " + dbName + " " + dbMajorVersion)
    }
  }
}

