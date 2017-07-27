package com.arcusys.valamis.learningpath.services.impl.utils

import com.arcusys.valamis.learningpath.models.{Goal, GoalStatuses, GoalTypes, UserGoalStatus}
import org.joda.time.DateTime

/**
  * Created by mminin on 23/03/2017.
  */
object UserStatusUtil {

  def getNewStatuses(goals: Seq[Goal],
                     userIds: Seq[Long],
                     now: DateTime,
                     startedDate: Option[DateTime] = None): Seq[UserGoalStatus] = {
    for {
      goal <- goals
      userId <- userIds
    } yield {
      getNewStatus(userId, goal, now, startedDate)
    }
  }

  def getNewStatus(userId: Long, goal: Goal, now: DateTime, startedDate: Option[DateTime] = None): UserGoalStatus = {
    val startDate = startedDate getOrElse now
    val endDate = goal.timeLimit.map(timeLimit => startDate plus timeLimit)
    //default counts will be rewrited in checker
    UserGoalStatus(userId, goal.id, getStartStatus(goal), startDate, now, 1, 0, endDate)
  }

  def getStartStatus(goal: Goal): GoalStatuses.Value = {
    goal.goalType match {
      case GoalTypes.Group => GoalStatuses.Undefined
      case GoalTypes.Lesson => GoalStatuses.Undefined
      case GoalTypes.LRActivity => GoalStatuses.InProgress
      case GoalTypes.Assignment => GoalStatuses.Undefined
      case GoalTypes.WebContent => GoalStatuses.InProgress
      case GoalTypes.TrainingEvent => GoalStatuses.Undefined
      case GoalTypes.Statement => GoalStatuses.InProgress
      case GoalTypes.Course => GoalStatuses.Undefined
    }
  }

}
