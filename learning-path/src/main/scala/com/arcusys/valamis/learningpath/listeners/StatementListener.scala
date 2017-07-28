package com.arcusys.valamis.learningpath.listeners

import com.arcusys.valamis.learningpath.models.StatementInfo
import com.arcusys.valamis.learningpath.tasks.TaskManager
import com.arcusys.valamis.learningpath.utils.{DbActions, DbActionsSupport}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by mminin on 20/03/2017.
  */
class StatementListener(val dbActions: DbActions,
                        taskManager: TaskManager)
                       (implicit executionContext: ExecutionContext)
  extends DbActionsSupport {

  def onStatementCreated(userId: Long,
                         statement: StatementInfo)
                        (implicit companyId: Long): Future[Unit] = {
    db.run {
      goalStatementDBIO.getByStatementAndNotCompletedUserStatus(
        statement.verbId,
        statement.objectId,
        userId
      )
    } map {
      _.foreach { goal =>
        taskManager.planStatementGoalCheckerTask(goal.goalId, userId, statement)
      }
    }
  }
}
