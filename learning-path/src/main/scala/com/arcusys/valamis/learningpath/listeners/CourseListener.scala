package com.arcusys.valamis.learningpath.listeners

import com.arcusys.valamis.learningpath.tasks.TaskManager
import com.arcusys.valamis.learningpath.utils.{DbActions, DbActionsSupport}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by pkornilov on 3/20/17.
  */
class CourseListener(val dbActions: DbActions,
                     taskManager: TaskManager)
                    (implicit executionContext: ExecutionContext)
  extends CompletedListener
    with DbActionsSupport {

  override def onCompleted(userId: Long, courseId: Long)
                          (implicit companyId: Long): Future[Unit] = {

    db.run {
      goalCourseDBIO.getByCourseIdAndNotCompletedUserStatus(courseId, userId)
    } map {
      _.foreach { goal =>
        taskManager.planCourseGoalCheckerTask(goal.goalId, userId)
      }
    }
  }
}
