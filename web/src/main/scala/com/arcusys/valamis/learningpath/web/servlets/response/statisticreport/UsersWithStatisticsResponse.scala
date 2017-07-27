package com.arcusys.valamis.learningpath.web.servlets.response.statisticreport

import com.arcusys.valamis.learningpath.models.{CertificateStatuses, GoalStatuses, UserLPStatus}
import com.arcusys.valamis.members.picker.model.UserInfo
import org.joda.time.DateTime


case class UsersWithStatisticsResponse(id: Long,
                                       name: String,
                                       logo: String,

                                       status: Option[CertificateStatuses.Value],
                                       statusDate: Option[DateTime],
                                       progress: Option[Double],

                                       statusToCount: Map[GoalStatuses.Value, Int]
                                      ) {
  def this(user: UserInfo,
           status: Option[UserLPStatus],
           goalStatusToCount: Map[GoalStatuses.Value, Int]) = {
    this(
      user.id,
      user.name,
      user.logo,
      status.map(_.status),
      status.map(_.modifiedDate),
      status.map(_.progress),
      goalStatusToCount
    )
  }
}
