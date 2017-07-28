package com.arcusys.valamis.learningpath.tasks

import com.arcusys.valamis.learningpath.models._
import com.arcusys.valamis.learningpath.services.CourseUserStatusService
import com.arcusys.valamis.learningpath.services.exceptions.CoursesIsNotDeployedError
import com.arcusys.valamis.learningpath.utils.{DbActions, DbActionsSupport}
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}

class CourseGoalChecker(val dbActions: DbActions,
                        courseUserStatusService: CourseUserStatusService,
                        val taskManager: TaskManager)
                       (implicit val executionContext: ExecutionContext)
  extends GoalCheckerBase
    with DbActionsSupport {

  override val goalType = GoalTypes.Course

  override def updateStatus(goal: Goal,
                            userGoalStatus: UserGoalStatus,
                            now: DateTime)
                           (implicit companyId: Long): Future[Unit] = {
    val endDate = goal.timeLimit.map(userGoalStatus.startedDate.withPeriodAdded(_, 1))

    val checkEndDateAction =
      if (endDate.exists(_ isBefore now)) {
        updateGoalProgress(goal, userGoalStatus, endDate.get, GoalStatuses.Failed)
      } else {
        updateGoalProgress(goal, userGoalStatus, now, GoalStatuses.InProgress)
      }

    (for {
      courseGoal <- db.run(goalCourseDBIO.get(goal.id)).map {
        _.getOrElse(throw new Exception("wrong goal type"))
      }

      status <- courseUserStatusService.getCourseStatusForUser(courseGoal.courseId,
        userGoalStatus.userId)

      _ <- if (status.isCompleted) {
        if (endDate.exists(_ isBefore status.date)) {
          checkEndDateAction
        } else {
          updateGoalProgress(goal, userGoalStatus, status.date, GoalStatuses.Success)
        }
      } else {
        checkEndDateAction
      }

    } yield {}) recoverWith {
      case _: CoursesIsNotDeployedError =>
        updateGoalProgress(goal, userGoalStatus, now, GoalStatuses.InProgress)
    }

  }


}
