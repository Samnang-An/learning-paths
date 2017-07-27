package com.arcusys.valamis.learningpath.tasks

import com.arcusys.valamis.learningpath.models._
import com.arcusys.valamis.learningpath.utils.{DbActions, DbActionsSupport}
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by mminin on 07/03/2017.
  */
class GroupsGoalChecker(val dbActions: DbActions,
                        val taskManager: TaskManager)
                       (implicit val executionContext: ExecutionContext)
  extends GoalCheckerBase
    with DbActionsSupport {

  override val goalType = GoalTypes.Group

  override protected def updateStatus(goal: Goal,
                                      userGoalStatus: UserGoalStatus,
                                      now: DateTime)
                                     (implicit companyId: Long): Future[Unit] = {
    for {
      goalGroup <- db.run(goalGroupDBIO.get(goal.id)).map {
        _.getOrElse(throw new Exception("wrong goal type"))
      }
      subGoals <- db.run(goalDBIO.getByVersionIdAndParentGroupId(goal.versionId, Some(goal.id)))
      subStatuses <- db.run(userGoalStatusDBIO.getByGoalGroupId(goal.id, userGoalStatus.userId))

      _ <- updateGroup(goal, goalGroup, userGoalStatus, subGoals, subStatuses, now)
    } yield {}
  }

  private def updateGroup(goal: Goal,
                          goalGroup: GoalGroup,
                          status: UserGoalStatus,
                          subGoals: Seq[Goal],
                          subStatuses: Seq[UserGoalStatus],
                          now: DateTime)
                         (implicit companyId: Long): Future[Unit] = {
    //TODO: validate sub statuses, if no status for goal then create it

    val (requiredCount, completedCount) = GroupsGoalChecker
      .getGroupGoalsCounts(goalGroup, subGoals, subStatuses)

    val complete = GroupsGoalChecker.isComplete(goalGroup, subGoals, subStatuses)

    val timeOut = goal.timeLimit.exists { limit =>
      status.startedDate.withPeriodAdded(limit, 1).isBeforeNow
    }

    if (timeOut) {
      updateGoalProgress(goal, status, now, GoalStatuses.Failed, requiredCount, completedCount)
    } else if (complete) {
      updateGoalProgress(goal, status, now, GoalStatuses.Success, requiredCount, completedCount)
    } else {
      updateGoalProgress(goal, status, now, GoalStatuses.InProgress, requiredCount, completedCount)
    }
  }
}

object GroupsGoalChecker {

  def isComplete(group: GoalGroup,
                 subGoals: Seq[Goal],
                 subStatuses: Seq[UserGoalStatus]): Boolean = {

    group.count match {
      case Some(targetCount) =>
        subStatuses.count(_.status == GoalStatuses.Success) >= targetCount
      case None =>
        subGoals
          .filter(!_.optional)
          .flatMap(g => subStatuses.find(_.goalId == g.id))
          .forall(_.status == GoalStatuses.Success)
    }
  }

  def getGroupGoalsCounts(group: GoalGroup,
                          subGoals: Seq[Goal],
                          subStatuses: Seq[UserGoalStatus]): (Int, Int) = {
    val subStatusesToCheck = group.count match {
      case Some(targetCount) =>
        //should be completed $targetCount goals
        //we check goals that are closest to success
        subStatuses
          .sortBy(s => s.requiredCount - s.completedCount)
          .take(targetCount)

      case None =>
        //should be completed all mandatory goals
        subGoals
          .filter(!_.optional)
          .flatMap(g => subStatuses.find(_.goalId == g.id))
    }

    val requiredCount = subStatusesToCheck.map(_.requiredCount).sum
    val completedCount = subStatusesToCheck.map(_.completedCount).sum

    (requiredCount, completedCount)
  }
}
