package com.arcusys.valamis.learningpath.web.servlets.response.statisticreport

import com.arcusys.valamis.learningpath.models.{CertificateStatuses, LPVersion, LearningPath}


object LPWithStatisticsResponse {

  def apply(learningPath: LearningPath,
            version: LPVersion,
            statusToCount: Map[CertificateStatuses.Value, Int],
            logoFilesPrefix: String) = {
    new LPWithStatisticsResponse(
      learningPath.id,
      version.title,
      version.logo.map(logo => s"$logoFilesPrefix/$logo"),
      statusToCount
    )
  }
}

case class LPWithStatisticsResponse(id: Long,
                                    title: String,
                                    logoUrl: Option[String],
                                    statusToCount: Map[CertificateStatuses.Value, Int])
