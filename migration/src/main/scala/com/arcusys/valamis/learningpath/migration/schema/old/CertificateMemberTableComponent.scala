package com.arcusys.valamis.learningpath.migration.schema.old

import com.arcusys.valamis.learningpath.migration.schema.old.model.{CertificateMember, MemberTypes}
import com.arcusys.valamis.learningpath.services.impl.tables.utils.{SlickProfile, TableHelper}

private[migration] trait CertificateMemberTableComponent
  extends CertificateTableComponent
    with TableHelper { self: SlickProfile =>

  import profile.api._

  implicit lazy val memberTypeMapper = enumerationIdMapper(MemberTypes)

  class CertificateMemberTable(tag: Tag) extends Table[CertificateMember](tag, "LEARN_CERTIFICATE_MEMBER") {
    def certificateId = column[Long]("CERTIFICATE_ID")
    def memberId = column[Long]("MEMBER_ID")
    def memberType = column[MemberTypes.Value]("MEMBER_TYPE")

    def * = (certificateId, memberId, memberType) <> (CertificateMember.tupled, CertificateMember.unapply)
  }

  val certificateMembers = TableQuery[CertificateMemberTable]
}
