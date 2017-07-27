package com.arcusys.valamis.learningpath.tasks

import com.arcusys.valamis.learningpath.models._
import org.joda.time.DateTime
import com.arcusys.valamis.learningpath.utils.{DbActions, DbActionsSupport}

import scala.concurrent.{ExecutionContext, Future}


class WebContentGoalChecker(val dbActions: DbActions,
                            val taskManager: TaskManager)
                           (implicit val executionContext: ExecutionContext)
    extends DbActionsSupport {

  import profile.api._

  def checkGoal(goalId: Long,
                userId: Long,
                viewed: Boolean)
               (implicit companyId: Long): Future[Unit] = {
    val action =
      for {
        status <- userGoalStatusDBIO.getStatus(userId, goalId)
        (goal, goalData) <- goalWebContentDBIO.getWithGoalInfoById(goalId).map {
          case Some(data) => data
          case _ => throw new TaskAbortException(s"no Statement goal with id: $goalId")
        }
        _ <- versionDBIO.getWithLearningPathById(goal.versionId) map {
          case Some((lp, _)) if !lp.activated =>
            throw new TaskAbortException(s"learning path deactivated, id: ${lp.id}")
          case _ =>
        }
        newStatus <- (status, goal) match {
          case (Some(userStatus), goal)
            if (viewed) =>
            updateStatus(goal, userId, userStatus.startedDate, completedCount = 1)
          case _ =>
            DBIO.successful(GoalStatuses.InProgress)
        }
      } yield {
        (newStatus, goal)
      }

    db.run(action.transactionally)
      .map { case (status, goal) => planNextTask(status, goal, userId) }
  }


  def updateStatus(goal: Goal,
                   userId: Long,
                   startDate: DateTime,
                   completedCount: Int)
                  (implicit companyId: Long): DBIO[GoalStatuses.Value] = {
    val now = DateTime.now
    val endDate = goal.timeLimit.map(startDate.withPeriodAdded(_, 1))

    val requiredCount = 1

    if (endDate.exists(_ isBefore now)) {
      userGoalStatusDBIO
        .updateStatus(userId, goal.id, GoalStatuses.Failed, now, requiredCount, completedCount)
        .map(_ => GoalStatuses.Failed)
    } else {
      userGoalStatusDBIO
        .updateStatus(userId, goal.id, GoalStatuses.Success, now, requiredCount, completedCount)
        .map(_ => GoalStatuses.Success)
    }
  }

  private def planNextTask(newStatus: GoalStatuses.Value,
                           goal: Goal,
                           userId: Long)
                          (implicit companyId: Long): Unit = {
    if (newStatus == GoalStatuses.Success || newStatus == GoalStatuses.Failed) {
      goal.groupId match {
        case Some(groupId) => taskManager.planGroupGoalCheckerTask(groupId, userId)
        case None => taskManager.planLearningPathCheckerTask(goal.versionId, userId)
      }
    }
  }
}
