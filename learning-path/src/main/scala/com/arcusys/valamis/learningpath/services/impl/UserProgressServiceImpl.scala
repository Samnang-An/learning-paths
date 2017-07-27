package com.arcusys.valamis.learningpath.services.impl

import com.arcusys.valamis.learningpath.models.{CertificateStatuses, GoalStatuses, UserGoalStatus, UserLPStatus}
import com.arcusys.valamis.learningpath.services.UserProgressService
import com.arcusys.valamis.learningpath.services.exceptions.NoLearningPathError
import com.arcusys.valamis.learningpath.utils.{DbActions, DbActionsSupport}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

/**
  * Created by mminin on 13/03/2017.
  */
class UserProgressServiceImpl(val dbActions: DbActions)
                             (implicit executionContext: ExecutionContext)
  extends UserProgressService
    with DbActionsSupport {


  override def getUserGoalsStatuses(learningPathId: Long,
                                    userId: Long)
                                   (implicit companyId: Long): Future[Seq[UserGoalStatus]] = {

    val action = for {
      (versionId, _) <- versionDBIO.getCurrentByLearningPathId(learningPathId)
        .map(_ getOrElse (throw new NoLearningPathError(learningPathId)))
      _ <- userMemberDBIO.hasByUserIdAndLearningPathIdQ(userId, learningPathId)
        .map(joined => if (!joined) throw new NoLearningPathError(learningPathId))
      statuses <- userGoalStatusDBIO.getByLearningPathAndUser(learningPathId, userId)
    } yield {
      statuses
    }

    db.run(action)
    //TODO: run bg checker for undefined statuses
  }

  override def getUserLPStatuses(learningPathId: Long,
                                 userId: Long)
                                (implicit companyId: Long): Future[Option[UserLPStatus]] = {
    val action = for {
      (versionId, version) <- versionDBIO.getCurrentByLearningPathId(learningPathId)
        .map(_ getOrElse (throw new NoLearningPathError(learningPathId)))
      status <- userLPStatusDBIO.getByUserAndLearningPath(version.learningPathId, userId)
    } yield {
      status
    }

    db.run(action)
  }

  override def getUsersLPStatuses(learningPathId: Long,
                                  userIds: Seq[Long])
                                 (implicit companyId: Long): Future[Seq[UserLPStatus]] = {
    val action = for {
      (versionId, version) <- versionDBIO.getCurrentByLearningPathId(learningPathId)
        .map(_ getOrElse (throw new NoLearningPathError(learningPathId)))
      status <- userLPStatusDBIO.getByUserIdsAndLearningPathId(userIds, version.learningPathId)
    } yield {
      status
    }

    db.run(action)
  }

  def getCountByGoalsStatuses(userId: Long,
                              status: GoalStatuses.Value)
                             (implicit companyId: Long): Future[Int] = {
    db.run {
      userGoalStatusDBIO.getCountByUserAndStatus(userId, status)
    }
  }

  def getCountsByLPStatuses(learningPathIds: Seq[Long])
                           (implicit companyId: Long): Future[Map[Long, Map[CertificateStatuses.Value, Int]]] = {
    db.run {
      userLPStatusDBIO.getGoalsStatusToCount(learningPathIds)
    } map {
      _.groupBy { case (lpId, _, _) => lpId }
        .mapValues(_ map { case (_, status, count) => (status, count) } toMap )
    }
  }

  def getUsersGoalsCounts(learningPathId: Long,
                          userIds: Seq[Long])
                         (implicit companyId: Long): Future[Map[Long, Map[GoalStatuses.Value, Int]]] = {
    db.run {
      userGoalStatusDBIO.getUsersGoalsStatusToCount(userIds, learningPathId)
    } map {
      _.groupBy { case (userId, _, _) => userId }
        .mapValues(_ map { case (_, status, count) => (status, count) } toMap )
    }
  }
}
