package com.arcusys.valamis.learningpath.listeners

import com.arcusys.valamis.learningpath.tasks.TaskManager
import com.arcusys.valamis.learningpath.utils.{DbActions, DbActionsSupport}

import scala.concurrent.{ExecutionContext, Future}

class AssignmentListener(val dbActions: DbActions,
                         taskManager: TaskManager)
                        (implicit executionContext: ExecutionContext)
  extends CompletedListener
    with DbActionsSupport {

  override def onCompleted(userId: Long, assignmentId: Long)
                        (implicit companyId: Long): Future[Unit] = {

    db.run {
      goalAssignmentDBIO.getByAssignmentIdAndNotCompletedUserStatus(assignmentId, userId)
    } map {
      _.foreach { goal =>
        taskManager.planAssignmentGoalCheckerTask(goal.goalId, userId)
      }
    }
  }
}
