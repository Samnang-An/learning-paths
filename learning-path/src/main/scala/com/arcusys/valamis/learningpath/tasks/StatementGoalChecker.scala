package com.arcusys.valamis.learningpath.tasks

import com.arcusys.valamis.learningpath.models.{GoalStatuses, _}
import com.arcusys.valamis.learningpath.utils.{DbActions, DbActionsSupport}
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by mminin on 07/03/2017.
  */
class StatementGoalChecker(val dbActions: DbActions,
                           val taskManager: TaskManager)
                          (implicit val executionContext: ExecutionContext)
  extends DbActionsSupport {

  import profile.api._

  private def isCheckable(status: GoalStatuses.Value) = {
    status == GoalStatuses.InProgress || status ==  GoalStatuses.Undefined
  }

  def checkGoal(goalId: Long,
                userId: Long,
                statement: StatementInfo)
               (implicit companyId: Long): Future[Unit] = {
    val action = for {
      status <- userGoalStatusDBIO.getStatus(userId, goalId).map {
        _.getOrElse(throw new TaskAbortException(s"no user goal state: goalId: $goalId, userId $userId"))
      }
      (goal, goalData) <- goalStatementDBIO.getWithGoalInfoById(goalId).map {
        case Some(data) => data
        case _ => throw new TaskAbortException(s"no Statement goal with id: $goalId")
      }
      _ <- versionDBIO.getWithLearningPathById(goal.versionId) map {
        case Some((lp, _)) if !lp.activated =>
          throw new TaskAbortException(s"learning path deactivated, id: ${lp.id}")
        case _ =>
      }

      newStatus <- status.status match {
        case GoalStatuses.InProgress | GoalStatuses.Undefined =>
          updateStatus(goal, goalData, userId, status.startedDate, statement)
        case _ =>
          DBIO.successful(GoalStatuses.Undefined)
      }
    } yield {
      (newStatus, goal)
    }

    db.run(action.transactionally)
      .map {
        case (status, goal) => planNextTask(status, goal, userId)
        case _ => Unit
      }
  }


  private def updateStatus(goal: Goal,
                           goalData: GoalStatement,
                           userId: Long,
                           startDate: DateTime,
                           statement: StatementInfo)
                          (implicit companyId: Long): DBIO[GoalStatuses.Value] = {
    val now = DateTime.now
    val endDate = goal.timeLimit.map(startDate.withPeriodAdded(_, 1))

    val isCorrectStatement =
      goalData.verbId == statement.verbId &&
        goalData.objectId == statement.objectId &&
        endDate.forall(_ isAfter statement.timeStamp)

    val newStatus = if (isCorrectStatement) {
      Some(GoalStatuses.Success)
    } else if (endDate.exists(_ isBefore now)) {
      Some(GoalStatuses.Failed)
    } else {
      None
    }

    newStatus map { status =>
      val requiredCount = 1
      val completedCount = if (status == GoalStatuses.Success) 1 else 0
      userGoalStatusDBIO.updateStatus(userId, goal.id, status, now, requiredCount, completedCount)
        .map(_ => status)
    } getOrElse {
      DBIO.successful(GoalStatuses.InProgress)
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
