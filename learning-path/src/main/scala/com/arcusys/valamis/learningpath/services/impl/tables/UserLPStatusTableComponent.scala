package com.arcusys.valamis.learningpath.services.impl.tables

import com.arcusys.valamis.learningpath.models.{CertificateStatuses, UserLPStatus}
import com.arcusys.valamis.learningpath.services.impl.tables.utils.{SlickProfile, TableHelper}
import org.joda.time.DateTime

/**
  * Created by mminin on 23/01/2017.
  */
trait UserLPStatusTableComponent extends TableHelper {
  self: SlickProfile
    with LeaningPathTableComponent
    with LPVersionTableComponent =>

  import profile.api._

  implicit lazy val lpStatusesMapper = enumerationIdMapper(CertificateStatuses)

  lazy val userLPStatusTQ = TableQuery[UserStatusTable]

  class UserStatusTable(tag: Tag) extends Table[UserLPStatus](tag, tblName("USR_STATUS"))
    with LearningPathLinkSupport
    with LPVersionLinkSupport {

    val userId = column[Long]("USER_ID")
    val status = column[CertificateStatuses.Value]("STATUS")
    val startedDate = column[DateTime]("STARTED_DATE")
    val modifiedDate = column[DateTime]("MODIFIED_DATE")
    val progress = column[Double]("PROGRESS")

    def pk = primaryKey("PK_LP_USER_STATUS", (learningPathId, userId))

    def lpIdx = index(idxName(tableName, "lp"), learningPathId)

    override def * = (userId, learningPathId, versionId, status, startedDate, modifiedDate, progress) <>
      (UserLPStatus.tupled, UserLPStatus.unapply)
  }

}
