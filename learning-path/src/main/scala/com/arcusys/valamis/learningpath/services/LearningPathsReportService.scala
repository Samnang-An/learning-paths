package com.arcusys.valamis.learningpath.services

import com.arcusys.valamis.learningpath.models._
import com.arcusys.valamis.learningpath.models.patternreport._
import com.arcusys.valamis.members.picker.model.UserInfo
import org.joda.time.DateTime

import scala.concurrent.Future


trait LearningPathsReportService {

  def getCertificates(courseId: Option[Long])
                     (implicit companyId: Long): Future[Seq[(LPVersion, GoalsSet)]]

  def getUsers(courseId: Option[Long], userIds: Seq[Long])
              (implicit companyId: Long): Future[Seq[(LPVersion, UserLPStatus)]]

  def getTotalStatus(courseId: Option[Long], userIds: Seq[Long])
                    (implicit companyId: Long): Future[Seq[(Long, Seq[(CertificateStatuses.Value, Int)])]]

  def getTotalGoalStatus(learningPathId: Long)
                        (implicit companyId: Long): Future[Seq[(Long, Seq[(GoalStatuses.Value, Int)])]]

  def getUserGoalStatuses(learningPathId: Long, userIds: Seq[Long])
                         (implicit companyId: Long): Future[Seq[Seq[UserGoalStatus]]]

  def getEndDate(certificate: LPVersion, userLPStatus: UserLPStatus): Option[DateTime]

  def getStatus(certificate: LPVersion, userLPStatus: UserLPStatus): PathsReportStatus.Value

  def getJoinedUserIds(courseId: Option[Long])(implicit companyId: Long): Future[Seq[Long]]
}
