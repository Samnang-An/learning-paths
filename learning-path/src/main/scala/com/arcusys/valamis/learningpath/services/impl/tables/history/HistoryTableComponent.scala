package com.arcusys.valamis.learningpath.services.impl.tables.history

import com.arcusys.valamis.learningpath.models.{CertificateStatuses, PeriodTypes}
import com.arcusys.valamis.learningpath.models.history.{LPSnapshot, UserStatusSnapshot}
import com.arcusys.valamis.learningpath.services.impl.tables.utils.{SlickProfile, TableHelper}
import org.joda.time.DateTime

trait HistoryTableComponent extends TableHelper {
  self: SlickProfile =>

  import profile.api._

  class LPHistoryTable(tag: Tag)
    extends Table[LPSnapshot](tag, "LEARN_CERTIFICATE_HSTRY") {

    implicit lazy val ValidPeriodTypeMapper = enumerationIdMapper(PeriodTypes)

    val learningPathId = column[Long]("CERTIFICATE_ID")
    val date = column[DateTime]("DATE")
    val isDeleted = column[Boolean]("IS_DELETED")

    val title = column[String]("TITLE")
    val isPermanent = column[Boolean]("IS_PERMANENT")

    val companyId = column[Long]("COMPANY_ID")
    val validPeriodType = column[PeriodTypes.PeriodType]("PERIOD_TPE")
    val validPeriod = column[Int]("VALID_PERIOD")
    val isPublished = column[Boolean]("IS_PUBLISHED")
    val scope = column[Option[Long]]("SCOPE")

    override def * = (
      learningPathId,
      date,
      isDeleted,
      title,
      isPermanent,
      companyId,
      validPeriodType,
      validPeriod,
      isPublished,
      scope) <> (LPSnapshot.tupled, LPSnapshot.unapply)
  }

  class UserStatusHistoryTable(tag: Tag)
    extends Table[UserStatusSnapshot](tag, "LEARN_CERT_STATE_HSTRY") {

    implicit lazy val ValidPeriodTypeMapper = enumerationIdMapper(CertificateStatuses)

    val certificateId = column[Long]("CERTIFICATE_ID")
    val userId = column[Long]("USER_ID")
    val date = column[DateTime]("DATE")
    val isDeleted = column[Boolean]("IS_DELETED")

    val status = column[CertificateStatuses.Value]("STATUS")

    override def * = (
      certificateId,
      userId,
      status,
      date,
      isDeleted) <> (UserStatusSnapshot.tupled, UserStatusSnapshot.unapply)
  }

  val userStatusHistoryTQ = TableQuery[UserStatusHistoryTable]
  val lpHistoryTQ = TableQuery[LPHistoryTable]
}