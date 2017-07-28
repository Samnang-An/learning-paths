package com.arcusys.valamis.learningpath.tasks

import com.arcusys.valamis.learningpath.models.{Goal, GoalStatuses, GoalTypes, UserGoalStatus}
import com.arcusys.valamis.learningpath.utils.{DbActions, DbActionsSupport}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Find undefined goals statuses and plan next steps to check it
  */
class UndefinedStatusesChecker(val dbActions: DbActions,
                               taskManager: TaskManager)
                              (implicit executionContext: ExecutionContext)
  extends DbActionsSupport {

  def checkLearningPath(learningPathId: Long)
                       (implicit companyId: Long): Future[Unit] = {

    db.run {
      for {
        _ <- learningPathDBIO.getById(learningPathId) map {
          case Some(lp) if !lp.activated =>
            throw new TaskAbortException(s"learning path deactivated, id: ${lp.id}")
          case _ =>
        }
        (versionId, version) <- versionDBIO.getCurrentByLearningPathId(learningPathId) map {
          case Some((versionId, version)) if version.published => (versionId, version)
          case _ => throw new TaskAbortException(s"learning path: $learningPathId not published")
        }
        states <- userGoalStatusDBIO.getWithGoalByVersionAndStatus(versionId, GoalStatuses.Undefined)
      } yield {
        (versionId, states)
      }
    } flatMap {
      case (versionId, states) =>
        if (states.nonEmpty) planStatusesCheckers(states)

        //TODO: may be checked twice, StatusesCheckersTask can run LearningPathStatusCheckerTask too
        planLearningPathStatusChecker(learningPathId, versionId)
    }
  }

  private def planStatusesCheckers(states: Seq[(Goal, UserGoalStatus)])
                                  (implicit companyId: Long): Future[Unit] = Future.successful {
    states foreach { case (goal, userStatus) =>
      taskManager.planGoalChecker(goal, userStatus.userId)
    }
  }

  private def planLearningPathStatusChecker(learningPathId: Long, versionId: Long)
                                           (implicit companyId: Long): Future[Unit] = {
    db.run(userMemberDBIO.getUserIdsByLearningPathId(learningPathId))
      .map {
        _.foreach(userId => taskManager.planLearningPathCheckerTask(versionId, userId))
      }
  }
}
