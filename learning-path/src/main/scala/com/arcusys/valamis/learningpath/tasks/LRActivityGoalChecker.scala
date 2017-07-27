package com.arcusys.valamis.learningpath.tasks

import com.arcusys.valamis.learningpath.models.{Goal, GoalStatuses, GoalTypes, UserGoalStatus}
import com.arcusys.valamis.learningpath.services.LRActivityTypeService
import com.arcusys.valamis.learningpath.utils.{DbActions, DbActionsSupport}
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by mminin on 07/03/2017.
  */
class LRActivityGoalChecker(val dbActions: DbActions,
                            lrActivityTypeService: LRActivityTypeService,
                            val taskManager: TaskManager)
                           (implicit val executionContext: ExecutionContext)
  extends GoalCheckerBase
    with DbActionsSupport {

  override val goalType = GoalTypes.LRActivity

  override protected def updateStatus(goal: Goal,
                           userGoalStatus: UserGoalStatus,
                           now: DateTime)
                          (implicit companyId: Long): Future[Unit] = {

    val endDate = goal.timeLimit.map(userGoalStatus.startedDate.withPeriodAdded(_, 1))

    //TODO don't compare end dates with now in all goal checker
    //because deadline checker runs every 30 minutes, so user can pass a goal
    //15 minutes before deadline and deadline checker can run after 15 minutes of deadline
    //and for some reason success event wasn't handled
    if (endDate.exists(_ isBefore now)) {
      updateGoalProgress(goal, userGoalStatus, endDate.get, GoalStatuses.Failed)
    } else {
      for {
        activityGoal <- db.run(goalActivityDBIO.get(goal.id)).map {
          _.getOrElse(throw new Exception("wrong goal type"))
        }
        userActivitiesCount <- lrActivityTypeService.getLRActivityCountByUser(
          userGoalStatus.userId,
          activityGoal.activityName,
          userGoalStatus.startedDate
        )
        _ <- if (userActivitiesCount < activityGoal.count) {
          updateGoalProgress(goal, userGoalStatus, now, GoalStatuses.InProgress)
        } else {
          //TODO: use last activity creation date instead now
          updateGoalProgress(goal, userGoalStatus, now, GoalStatuses.Success)
        }
      } yield {}
    }
  }

}
