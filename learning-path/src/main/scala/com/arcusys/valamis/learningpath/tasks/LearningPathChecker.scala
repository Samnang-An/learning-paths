package com.arcusys.valamis.learningpath.tasks

import com.arcusys.valamis.learningpath.listeners.UserLPStatusListener
import com.arcusys.valamis.learningpath.models.{CertificateStatuses, _}
import com.arcusys.valamis.learningpath.services.impl.UserLPStatusModelListener
import com.arcusys.valamis.learningpath.utils.{DbActions, DbActionsSupport}
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}

object LearningPathChecker {

  def getStatus(goals: Seq[Goal], goalsStatuses: Seq[UserGoalStatus]): (CertificateStatuses.Value, Double) = {
    val isNoGoals = goals.isEmpty
    if (isNoGoals) {
      (CertificateStatuses.InProgress, 0)
    } else {
      val mandatoryGoals = goals.filterNot(_.optional)
      val mandatory = mandatoryGoals
        .flatMap(g => goalsStatuses.find(_.goalId == g.id))

      val completedCount = mandatory.map(_.completedCount).sum
      val requiredCount = mandatory.map(_.requiredCount).sum

      val progress = if (requiredCount == 0) {
        1.0
      } else {
        completedCount.asInstanceOf[Double] / requiredCount
      }
      val mandatorySuccessCount = mandatory.count(_.status == GoalStatuses.Success)

      val isAllSuccess = mandatorySuccessCount == mandatory.length

      if (mandatory.exists(_.status == GoalStatuses.Failed)) {
        (CertificateStatuses.Failed, progress)
      } else if (isAllSuccess) {
        (CertificateStatuses.Success, progress)
      } else {
        (CertificateStatuses.InProgress, progress)
      }
    }
  }
}

/**
  * Check LP status for user,
  * find root goals status and update LP status
  */
class LearningPathChecker(val dbActions: DbActions,
                          userLPStatusModelListener: UserLPStatusModelListener,
                          userLpStatusListener: UserLPStatusListener)
                         (implicit val executionContext: ExecutionContext)
  extends DbActionsSupport {

  import com.arcusys.valamis.learningpath.tasks.LearningPathChecker._
  import profile.api._

  def checkLearningPath(versionId: Long, userId: Long)
                       (implicit companyId: Long): Future[Unit] = {
    val action = for {
      (lp, version) <- versionDBIO.getWithLearningPathById(versionId) map {
        case None =>
          throw new TaskAbortException(s"no learning path version: $versionId")
        case Some((_, version)) if !version.published =>
          throw new TaskAbortException(s"learning path version: $versionId not published")
        case Some((lp, _)) if !lp.activated =>
          throw new TaskAbortException(s"learning path deactivated, id: ${lp.id}")
        case Some((lp, version)) => (lp, version)
      }
      oldStatus <- getLearningPathStatus(lp.id, userId)
      newStatus <- oldStatus match {
        case s if s.status == CertificateStatuses.InProgress =>
          updateStatus(s)
        case _ => DBIO.successful(oldStatus)
      }
    } yield {
      (oldStatus, newStatus, lp, version)
    }

    db.run(action.transactionally)
      .flatMap { case (oldStatus, newStatus, lp, version) =>
        if (oldStatus.modifiedDate == newStatus.modifiedDate) {
          Future.successful {}
        } else {
          newStatus.status match {
            case CertificateStatuses.Success if oldStatus.status != CertificateStatuses.Success =>
              userLpStatusListener.onCompleted(userId, lp, version)
            case CertificateStatuses.Failed if oldStatus.status != CertificateStatuses.Failed =>
              userLpStatusListener.onFailed(userId, lp, version)
            case _ =>
          }
          userLPStatusModelListener.onChanged(newStatus)
        }
      }
  }

  private def getLearningPathStatus(learningPathId: Long, userId: Long) = {
    userLPStatusDBIO.getByUserAndLearningPath(learningPathId, userId).map {
      _.getOrElse {
        throw new TaskAbortException(s"no user progress in learning path: $learningPathId, userId $userId")
      }
    }
  }

  private def updateStatus(userLPStatus: UserLPStatus)
                          (implicit companyId: Long): DBIO[UserLPStatus] = {
    val now = DateTime.now
    val versionId = userLPStatus.versionId
    val userId = userLPStatus.userId

    for {
      rootGoals <- goalDBIO.getByVersionIdAndParentGroupId(versionId, parentGroupId = None)
      goalsStatuses <- userGoalStatusDBIO.getByUserIdAndGoalIds(userId, rootGoals.map(_.id))
      newLPStatus <- {
        val (status, progress) = getStatus(rootGoals, goalsStatuses)
        val newStatus = userLPStatus.copy(status = status, progress = progress, modifiedDate = now)

        userLPStatusDBIO.updateStatus(newStatus).map(_ => newStatus)
      }
    } yield {
      newLPStatus
    }
  }

}

