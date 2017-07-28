package com.arcusys.valamis.learningpath.listeners

import com.arcusys.valamis.learningpath.tasks.TaskManager
import com.arcusys.valamis.learningpath.utils.{DbActions, DbActionsSupport}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by mminin on 09/03/2017.
  */
class LRActivityListener(val dbActions: DbActions,
                         taskManager: TaskManager)
                        (implicit executionContext: ExecutionContext)
  extends DbActionsSupport {

  def onLRActivityCreated(userId: Long, activityName: String)
                         (implicit companyId: Long): Future[Unit] = {

    db.run {
      goalActivityDBIO.getByActivityNameAndNotCompletedUserStatus(activityName, userId)
    } map {
      _.foreach { goal =>
        taskManager.planLRActivityGoalCheckerTask(goal.goalId, userId)
      }
    }
  }
}
