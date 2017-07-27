package com.arcusys.valamis.learningpath.tasks

import com.arcusys.valamis.learningpath.models.{Goal, GoalStatuses, GoalTypes, UserGoalStatus}
import com.arcusys.valamis.learningpath.utils.DbActionsSupport
import org.joda.time.DateTime
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by mminin on 09/03/2017.
  */
trait GoalCheckerBase {
  self: DbActionsSupport =>

  implicit val executionContext: ExecutionContext

  val db: JdbcBackend#DatabaseDef
  val profile: JdbcProfile
  val taskManager: TaskManager
  val goalType: GoalTypes.Value

  protected def updateStatus(goal: Goal, userGoalStatus: UserGoalStatus, now: DateTime)
                            (implicit companyId: Long): Future[Unit]

  def checkGoal(goalId: Long, userId: Long,
                nowOpt: Option[DateTime] = None)//passing now from outside is used in tests
               (implicit companyId: Long): Future[Unit] = {
    val now = nowOpt getOrElse DateTime.now

    for {
      (goal, goalStatus) <- getGoalWithStatus(goalId, userId)
      _ <- goalStatus.status match {
        case GoalStatuses.Success | GoalStatuses.Failed => Future.successful({})
        case _ => updateStatus(goal, goalStatus, now)
      }
    } yield {}
  }

  private def getGoalWithStatus(goalId: Long, userId: Long): Future[(Goal, UserGoalStatus)] = {
    db.run {
      for {
        status <- userGoalStatusDBIO.getStatus(userId, goalId).map {
          _.getOrElse(throw new TaskAbortException(s"no user goal state: goalId: $goalId, userId $userId"))
        }
        goal <- goalDBIO.get(goalId).map {
          case Some(goal) if goal.goalType == goalType => goal
          case _ => throw new TaskAbortException(s"no ${goalType.toString} goal with id: $goalId")
        }
        _ <- versionDBIO.getWithLearningPathById(goal.versionId) map {
          case Some((lp, _)) if !lp.activated =>
            throw new TaskAbortException(s"learning path deactivated, id: ${lp.id}")
          case _ =>
        }
      } yield {
        (goal, status)
      }
    }
  }

  protected def updateGoalProgress(goal: Goal,
                                   userGoalStatus: UserGoalStatus,
                                   now: DateTime,
                                   status: GoalStatuses.Value)
                                  (implicit companyId: Long): Future[Unit] = {
    val requiredCount = 1
    val completedCount = if (status == GoalStatuses.Success) 1 else 0
    updateGoalProgress(goal, userGoalStatus, now, status, requiredCount, completedCount)
  }

  protected def updateGoalProgress(goal: Goal,
                                   userGoalStatus: UserGoalStatus,
                                   now: DateTime,
                                   status: GoalStatuses.Value,
                                   requiredCount: Int,
                                   completedCount: Int)
                                  (implicit companyId: Long): Future[Unit] = {
    db.run {
      userGoalStatusDBIO
        .updateStatus(userGoalStatus.userId, goal.id, status, now, requiredCount, completedCount)
    } map { _ =>
      if (userGoalStatus.status != status
        || userGoalStatus.requiredCount != requiredCount
        || userGoalStatus.completedCount != completedCount
      ) onGoalStatusChanged(goal, userGoalStatus)
    }
  }

  protected def onGoalStatusChanged(goal: Goal, userGoalStatus: UserGoalStatus)
                                   (implicit companyId: Long): Unit = {
    goal.groupId match {
      case Some(groupId) =>
        taskManager.planGroupGoalCheckerTask(groupId, userGoalStatus.userId)
      case None =>
        taskManager.planLearningPathCheckerTask(goal.versionId, userGoalStatus.userId)
    }
  }
}
