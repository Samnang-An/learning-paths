package com.arcusys.valamis.learningpath.web.servlets.response

import com.arcusys.valamis.learningpath.models.{Goal, GoalGroup, GoalTypes}
import org.joda.time.{DateTime, Period}

/**
  * Created by mminin on 20/02/2017.
  */
object GoalsGroupResponse {
  def apply(x: (Goal, GoalGroup)): GoalsGroupResponse = {
    apply(x._1, x._2)
  }

  def apply(goal: Goal, group: GoalGroup): GoalsGroupResponse = {
    GoalsGroupResponse(goal.id,
      goal.groupId,
      goal.indexNumber,
      goal.timeLimit,
      goal.optional,
      goal.modifiedDate,
      group.title,
      group.count
    )
  }
}

case class GoalsGroupResponse(id: Long,
                              groupId: Option[Long],
                              indexNumber: Int,
                              timeLimit: Option[Period],
                              optional: Boolean,
                              modifiedDate: DateTime,
                              title: String,
                              count: Option[Int],
                              goalType: GoalTypes.Value = GoalTypes.Group
                             ) extends GoalResponse

