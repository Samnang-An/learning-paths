package com.arcusys.valamis.learningpath.migration.schema.old

import com.arcusys.valamis.learningpath.migration.schema.old.model.CertificateState
import com.arcusys.valamis.learningpath.models.CertificateStatuses
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickProfile
import org.joda.time.DateTime


private[migration] trait CertificateStateTableComponent extends CertificateTableComponent { self: SlickProfile =>

  import profile.api._

    class CertificateStateTable(tag: Tag) extends Table[CertificateState](tag, "LEARN_CERT_STATE") {
      implicit lazy val CertificateStatusTypeMapper = enumerationMapper(CertificateStatuses)

      def userId = column[Long]("USER_ID")
      def status = column[CertificateStatuses.Value]("STATE")
      def statusAcquiredDate = column[DateTime]("STATE_ACQUIRED_DATE")

      def userJoinedDate = column[DateTime]("USER_JOINED_DATE")
      def certificateId = column[Long]("CERTIFICATE_ID")

      def * = (userId, status, statusAcquiredDate, userJoinedDate, certificateId) <>
        (CertificateState.tupled, CertificateState.unapply)

    }

    val certificateStates = TableQuery[CertificateStateTable]
}
