package com.arcusys.valamis.learningpath.tasks

import com.arcusys.valamis.learningpath.models._
import com.arcusys.valamis.learningpath.services.LessonService
import com.arcusys.valamis.learningpath.services.exceptions.{AssignmentIsNotDeployedError, LessonsIsNotDeployedError}
import com.arcusys.valamis.learningpath.utils.{DbActions, DbActionsSupport}
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by mminin on 07/03/2017.
  */
class LessonGoalChecker(val dbActions: DbActions,
                        lessonService: LessonService,
                        val taskManager: TaskManager)
                       (implicit val executionContext: ExecutionContext)
  extends GoalCheckerBase
    with DbActionsSupport {

  override val goalType = GoalTypes.Lesson

  protected override def updateStatus(goal: Goal,
                                      userGoalStatus: UserGoalStatus,
                                      now: DateTime)
                                     (implicit companyId: Long): Future[Unit] = {

    val endDate = goal.timeLimit.map(userGoalStatus.startedDate.withPeriodAdded(_, 1))

    if (endDate.exists(_ isBefore now)) {
      //TODO take into account lesson completed date
      updateGoalProgress(goal, userGoalStatus, endDate.get, GoalStatuses.Failed)
    } else {
      (for {
        lessonGoal <- db.run(goalLessonDBIO.get(goal.id)).map {
          _.getOrElse(throw new Exception("wrong goal type"))
        }

        isCompleted <- lessonService.isCompleted(lessonGoal.lessonId, userGoalStatus.userId)

        _ <- if (isCompleted) {
          updateGoalProgress(goal, userGoalStatus, now, GoalStatuses.Success)
        } else {
          updateGoalProgress(goal, userGoalStatus, now, GoalStatuses.InProgress)
        }

      } yield {}) recoverWith {
        case _: LessonsIsNotDeployedError =>
          updateGoalProgress(goal, userGoalStatus, now, GoalStatuses.InProgress)
      }
    }
  }
}
