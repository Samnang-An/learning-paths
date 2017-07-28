package com.arcusys.valamis.learningpath.services.impl.tables

import com.arcusys.valamis.learningpath.models.LPVersion
import com.arcusys.valamis.learningpath.services.impl.tables.utils.{SlickProfile, TableHelper}
import org.joda.time.{DateTime, Period}

/**
  * Created by mminin on 23/01/2017.
  */
trait LPVersionTableComponent extends TableHelper {
  self: SlickProfile
    with LeaningPathTableComponent =>

  import profile.api._

  lazy val versionTQ = TableQuery[LPVersionTable]

  trait LPVersionLinkSupport {
    self: Table[_] =>
    val versionId = column[Long]("VERSION_ID")

    def version = foreignKey(fkName(tableName, versionTQ.baseTableRow.tableName),
      versionId, versionTQ)(_.id)
  }

  class LPVersionTable(tag: Tag) extends Table[(Long, LPVersion)](tag, tblName("VERSION"))
    with IdentitySupport
    with LearningPathLinkSupport {

    val title = column[String]("TITLE", O.Length(titleSize, varying = true))
    val description = column[Option[String]]("DESCRIPTION", O.Length(descriptionSize, varying = true))
    val logo = column[Option[String]]("LOGO", O.Length(255, varying = true))

    val courseId = column[Option[Long]]("COURSE_ID")

    val published = column[Boolean]("PUBLISHED")
    val createdDate = column[DateTime]("CREATED_DATE")
    val modifiedDate = column[DateTime]("MODIFIED_DATE")

    val validPeriod = column[Option[Period]]("VALID_PERIOD")
    val expiringPeriod = column[Option[Period]]("EXPIRING_PERIOD")

    val openBadgesEnabled = column[Boolean]("OPEN_BADGES_ENABLED")
    val openBadgesDescription = column[Option[String]]("OPEN_BADGES_DESCRIPTION", O.Length(255, varying = true))

      def lpIdx = index(idxName(tableName, "lp"), learningPathId)

    override def * = (id, (learningPathId, title, description, logo,
      courseId, validPeriod, expiringPeriod, openBadgesEnabled, openBadgesDescription,
      published, createdDate, modifiedDate)).shaped <> ( {
      case (id_, data) => (id_, LPVersion.tupled.apply(data))
    }, {
      e: (Long, LPVersion) => Some((e._1, LPVersion.unapply(e._2).get))
    })

    def properties = (learningPathId, title, description, logo,
      courseId, validPeriod, expiringPeriod, openBadgesEnabled, openBadgesDescription,
      published, createdDate, modifiedDate) <>
      (LPVersion.tupled, LPVersion.unapply)
  }

}
