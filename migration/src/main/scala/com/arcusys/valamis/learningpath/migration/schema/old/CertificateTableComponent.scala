package com.arcusys.valamis.learningpath.migration.schema.old

import com.arcusys.valamis.learningpath.migration.schema.old.model.{Certificate, PeriodTypes}
import com.arcusys.valamis.learningpath.services.impl.tables.utils.{SlickProfile, TableHelper}
import org.joda.time.DateTime

private[migration] trait CertificateTableComponent extends LongKeyTableComponent with TableHelper { self: SlickProfile =>

  import profile.api._

  //used for migration test
  class CertificateTableWithoutAutoInc(tag: Tag) extends CertificateTable(tag) {
    override def id = column[Long]("ID", O.PrimaryKey)
  }

  class CertificateTable(tag: Tag) extends LongKeyTable[Certificate](tag, "LEARN_CERTIFICATE") {
    implicit lazy val ValidPeriodTypeMapper = enumerationMapper(PeriodTypes)

    def title = column[String]("TITLE")
    def description = column[String]("DESCRIPTION", O.Length(2000, varying = true))
    def logo = column[String]("LOGO")
    def isPermanent = column[Boolean]("IS_PERMANENT")
    def isPublishBadge = column[Boolean]("IS_PUBLISH_BADGE")
    def shortDescription = column[String]("SHORT_DESCRIPTION")
    def companyId = column[Long]("COMPANY_ID")
    def validPeriodType = column[PeriodTypes.PeriodType]("PERIOD_TPE")
    def validPeriod = column[Int]("VALID_PERIOD")
    def createdAt = column[DateTime]("CREATED_AT")
    def activationDate = column[Option[DateTime]]("ACTIVATION_DATE")
    def isActive = column[Boolean]("IS_ACTIVE")
    def scope = column[Option[Long]]("SCOPE")

    def * = (
      id,
      title,
      description,
      logo,
      isPermanent,
      isPublishBadge,
      shortDescription,
      companyId,
      validPeriodType,
      validPeriod,
      createdAt,
      activationDate,
      isActive,
      scope) <> (Certificate.tupled, Certificate.unapply)
  }

  val certificates = TableQuery[CertificateTable]
  val certificatesTQWithoutAutoInc = TableQuery[CertificateTableWithoutAutoInc]
}