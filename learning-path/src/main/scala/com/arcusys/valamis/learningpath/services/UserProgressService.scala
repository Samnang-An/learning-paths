package com.arcusys.valamis.learningpath.services

import com.arcusys.valamis.learningpath.models.{CertificateStatuses, GoalStatuses, UserGoalStatus, UserLPStatus}

import scala.concurrent.Future

trait UserProgressService {

  def getUserGoalsStatuses(learningPathId: Long,
                           userId: Long)
                          (implicit companyId: Long): Future[Seq[UserGoalStatus]]

  def getUserLPStatuses(learningPathId: Long,
                        userId: Long)
                       (implicit companyId: Long): Future[Option[UserLPStatus]]

  def getUsersLPStatuses(learningPathId: Long,
                         userIds: Seq[Long])
                        (implicit companyId: Long): Future[Seq[UserLPStatus]]

  def getUsersGoalsCounts(learningPathId: Long,
                          userIds: Seq[Long])
                         (implicit companyId: Long): Future[Map[Long, Map[GoalStatuses.Value, Int]]]

  def getCountsByLPStatuses(learningPathIds: Seq[Long])
                           (implicit companyId: Long): Future[Map[Long, Map[CertificateStatuses.Value, Int]]]

  def getCountByGoalsStatuses(userId: Long,
                              status: GoalStatuses.Value)
                             (implicit companyId: Long): Future[Int]

}