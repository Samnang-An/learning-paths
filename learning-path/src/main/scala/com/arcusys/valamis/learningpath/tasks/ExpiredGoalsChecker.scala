package com.arcusys.valamis.learningpath.tasks

import com.arcusys.valamis.learningpath.utils.{DbActions, DbActionsSupport}
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}

/**
  * Find expired goals and run checkers
  */
class ExpiredGoalsChecker(val dbActions: DbActions,
                          val taskManager: TaskManager)
                         (implicit val executionContext: ExecutionContext)
  extends DbActionsSupport {

  def check(versionId: Long, now: DateTime)
           (implicit companyId: Long): Future[Unit] = {

    db.run {
      for {
        _ <- versionDBIO.getWithLearningPathById(versionId) map {
          case None =>
            throw new TaskAbortException(s"no learning path version: $versionId")
          case Some((lp, version)) if !version.published =>
            throw new TaskAbortException(s"learning path version: $versionId not published")
          case Some((lp, version)) if !lp.activated =>
            throw new TaskAbortException(s"learning path deactivated, id: ${lp.id}")
          case Some((lp, version)) => (lp, version)
        }
        expiredGoals <- userGoalStatusDBIO.getByExpiredByVersionId(versionId, now)
      } yield {
        expiredGoals
      }
    } map { expiredGoals =>
      expiredGoals foreach { case (goal, userStatus) =>
        taskManager.planGoalChecker(goal, userStatus.userId)
      }
    }
  }
}
