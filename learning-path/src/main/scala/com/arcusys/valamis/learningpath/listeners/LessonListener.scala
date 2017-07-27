package com.arcusys.valamis.learningpath.listeners

import com.arcusys.valamis.learningpath.tasks.TaskManager
import com.arcusys.valamis.learningpath.utils.{DbActions, DbActionsSupport}

import scala.concurrent.{ExecutionContext, Future}

class LessonListener(val dbActions: DbActions,
                     taskManager: TaskManager)
                    (implicit executionContext: ExecutionContext)
  extends CompletedListener
    with DbActionsSupport {

  override def onCompleted(userId: Long, lessonId: Long)
                         (implicit companyId: Long): Future[Unit] = {

    db.run {
      goalLessonDBIO.getByLessonIdAndNotCompletedUserStatus(lessonId, userId)
    } map {
      _.foreach { goal =>
        taskManager.planLessonGoalCheckerTask(goal.goalId, userId)
      }
    }
  }
}
