package com.arcusys.valamis.learningpath.web.servlets.response

import com.arcusys.valamis.learningpath.models.{CertificateStatuses, LPWithInfo}
import org.joda.time.{DateTime, Period}

object LPForMemberResponse {
  def apply(data: LPWithInfo,
            logoFilesPrefix: String): LPForMemberResponse = {
    LPForMemberResponse(
      data.learningPath.id,
      data.learningPath.activated,
      data.learningPath.currentVersionId,
      data.versionProperties.published,
      data.versionProperties.title,
      data.versionProperties.description,
      data.versionProperties.logo.map(logo => s"$logoFilesPrefix/$logo"),
      data.versionProperties.courseId,
      data.versionProperties.validPeriod,
      data.versionProperties.expiringPeriod,
      data.versionProperties.openBadgesEnabled,
      data.versionProperties.openBadgesDescription,
      data.versionProperties.createdDate,
      data.versionProperties.modifiedDate,
      data.userMembersCount,
      data.goalsCount,
      data.learningPath.hasDraft,
      data.userStatus.map(_.status),
      data.userStatus.map(_.modifiedDate),
      data.userStatus.map(_.progress),
      data.userStatus.map(_.versionId)
    )
  }
}

case class LPForMemberResponse(id: Long,
                               activated: Boolean,
                               currentVersionId: Option[Long],
                               published: Boolean,

                               title: String,
                               description: Option[String],
                               logoUrl: Option[String],

                               courseId: Option[Long],
                               validPeriod: Option[Period],
                               expiringPeriod: Option[Period],

                               openBadgesEnabled: Boolean,
                               openBadgesDescription: Option[String],

                               createdDate: DateTime,
                               modifiedDate: DateTime,

                               userMembersCount: Int,
                               goalsCount: Int,
                               hasDraft: Boolean,

                               status: Option[CertificateStatuses.Value],
                               statusModifiedDate: Option[DateTime],
                               progress: Option[Double],
                               statusVersionId: Option[Long]
                              )
