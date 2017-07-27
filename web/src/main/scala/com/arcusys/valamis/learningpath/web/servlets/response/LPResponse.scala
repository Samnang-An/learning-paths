package com.arcusys.valamis.learningpath.web.servlets.response

import com.arcusys.valamis.learningpath.models.{LPVersion, LearningPath}
import org.joda.time.{DateTime, Period}

/**
  * Created by mminin on 30/01/2017.
  */
object LPResponse {
  def apply(learningPath: LearningPath,
            version: LPVersion,
            logoFilesPrefix: String): LPResponse = {
    LPResponse(
      learningPath.id,
      learningPath.activated,
      learningPath.currentVersionId,
      learningPath.hasDraft,
      version.published,
      version.title,
      version.description,
      version.logo.map(logo => s"$logoFilesPrefix/$logo"),
      version.courseId,
      version.validPeriod,
      version.expiringPeriod,
      version.openBadgesEnabled,
      version.openBadgesDescription,
      version.createdDate,
      version.modifiedDate
    )
  }
}

case class LPResponse(id: Long,
                      activated: Boolean,
                      currentVersionId: Option[Long],
                      hasDraft: Boolean,
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
                      modifiedDate: DateTime)
