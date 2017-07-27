package com.arcusys.valamis.learningpath.services.impl.tables.utils

import java.sql.{PreparedStatement, ResultSet}

import com.github.tototoshi.slick.converter.JodaDateTimeSqlTimestampConverter
import org.joda.time.{DateTime, Period}
import slick.profile.RelationalProfile

import slick.driver.MySQLDriver

trait TableHelper {
  self: SlickProfile =>

  import profile.api._

  val idName = "ID"
  val tableNamePrefix = "VLP_"

  val titleSize = 512
  val descriptionSize = 2000

  val nameSizeLimit = 30

  private def checkLengthAndReturn(name: String) = {
    assert(name.length <= nameSizeLimit, s"Name '$name' is too long: ${name.length}")
    name
  }

  def tblName(name: String): String = checkLengthAndReturn(tableNamePrefix + name)

  def fkName(fromName: String, toName: String) = {
    val from = fromName.replace(tableNamePrefix, "")
    val to = toName.replace(tableNamePrefix, "")
    checkLengthAndReturn(s"FK_${from}_TO_${to}")
  }

  def idxName(fromName: String, name: String) = {
    val from = fromName.replace(tableNamePrefix, "")
    checkLengthAndReturn(s"IDX_${from}_${name}")
  }

  trait IdentitySupport {
    self: Table[_] =>
    val id = column[Long](idName, O.PrimaryKey, O.AutoInc)
  }

  //TODO: use last version of tototoshi.slick.converter, it contains many fixes
  implicit lazy val jodaMapper =
    new profile.DriverJdbcType[DateTime] with JodaDateTimeSqlTimestampConverter {
      def zero = new DateTime(0L)
      def sqlType = java.sql.Types.TIMESTAMP
      override def sqlTypeName(size: Option[RelationalProfile.ColumnOption.Length]) =  profile match {
        case driver: MySQLDriver =>
          //default DATETIME in mysql loses seconds parts,
          //DATETIME(6) equals standard SQL DATETIME
          //https://dev.mysql.com/doc/refman/5.7/en/fractional-seconds.html
          "DATETIME"
        case _ => profile.columnTypes.timestampJdbcType.sqlTypeName(size)
      }
      override def setValue(v: DateTime, p: PreparedStatement, idx: Int): Unit =
        p.setTimestamp(idx, toSqlType(v))
      override def getValue(r: ResultSet, idx: Int): DateTime =
        fromSqlType(r.getTimestamp(idx))
      override def updateValue(v: DateTime, r: ResultSet, idx: Int): Unit =
        r.updateTimestamp(idx, toSqlType(v))
      override def valueToSQLLiteral(value: DateTime) = profile.columnTypes.timestampJdbcType.valueToSQLLiteral(toSqlType(value))
    }

  //TODO: set length limit for string column
  implicit lazy val jodaPeriodMapper = MappedColumnType.base[Period, String](
    e => e.toString,
    s => Period.parse(s)
  )

  def enumerationIdMapper[T <: Enumeration](enum: T) = MappedColumnType.base[enum.Value, Int](
    e => e.id,
    v => enum(v)
  )

  def enumerationMapper[T <: Enumeration](enum: T) = MappedColumnType.base[enum.Value, String](
    e => e.toString,
    s => enum.withName(s)
  )
}
