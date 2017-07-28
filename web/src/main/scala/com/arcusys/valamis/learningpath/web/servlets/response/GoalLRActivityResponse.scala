package com.arcusys.valamis.learningpath.web.servlets.response

import com.arcusys.valamis.learningpath.models.{Goal, GoalLRActivity, GoalTypes}
import org.joda.time.{DateTime, Period}

/**
  * Created by mminin on 20/02/2017.
  */
object GoalLRActivityResponse {
  def apply(x: (Goal, GoalLRActivity)): GoalLRActivityResponse = {
    apply(x._1, x._2)
  }

  def apply(goal: Goal, goalData: GoalLRActivity): GoalLRActivityResponse = {
    GoalLRActivityResponse(goal.id,
      goal.groupId,
      goal.indexNumber,
      goal.timeLimit,
      goal.optional,
      goal.modifiedDate,
      goalData.activityName,
      goalData.count
    )
  }
}

case class GoalLRActivityResponse(id: Long,
                                  groupId: Option[Long],
                                  indexNumber: Int,
                                  timeLimit: Option[Period],
                                  optional: Boolean,
                                  modifiedDate: DateTime,
                                  activityName: String,
                                  count: Int,
                                  goalType: GoalTypes.Value = GoalTypes.LRActivity
                                 ) extends GoalResponse

