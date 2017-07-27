package com.arcusys.valamis.learningpath.web.servlets.response

import com.arcusys.valamis.learningpath.models.{CertificateStatuses, UserLPStatus}
import com.arcusys.valamis.members.picker.model.{IdAndName, Member, UserInfo}
import org.joda.time.DateTime


case class UserMemberResponse(id: Long,
                              name: String,
                              logo: String,
                              groups: Seq[IdAndName],
                              roles: Seq[IdAndName],
                              organizations: Seq[IdAndName],
                              membershipInfo: Seq[Member],
                              status: Option[CertificateStatuses.Value],
                              statusDate: Option[DateTime],
                              progress: Option[Double],
                              statusVersionId: Option[Long]
                             ) {
  def this(user: UserInfo, status: Option[UserLPStatus]) = {
    this(
      user.id,
      user.name,
      user.logo,
      user.groups,
      user.roles,
      user.organizations,
      user.membershipInfo,
      status.map(_.status),
      status.map(_.modifiedDate),
      status.map(_.progress),
      status.map(_.versionId)
    )
  }
}
