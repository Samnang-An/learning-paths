package com.arcusys.valamis.learningpath.listeners

import com.arcusys.valamis.learningpath.tasks.TaskManager
import com.arcusys.valamis.learningpath.utils.{DbActions, DbActionsSupport}

import scala.concurrent.{ExecutionContext, Future}

class TrainingEventListener(val dbActions: DbActions,
                            taskManager: TaskManager)
                           (implicit executionContext: ExecutionContext)
  extends CompletedListener
    with DbActionsSupport {

  override def onCompleted(userId: Long, eventId: Long)
                        (implicit companyId: Long): Future[Unit] = {

    db.run {
      goalTrainingEventDBIO.getByTrainingEventIdAndNotCompletedUserStatus(eventId, userId)
    } map {
      _.foreach { goal =>
        taskManager.planTrainingEventGoalCheckerTask(goal.goalId, userId)
      }
    }
  }
}
