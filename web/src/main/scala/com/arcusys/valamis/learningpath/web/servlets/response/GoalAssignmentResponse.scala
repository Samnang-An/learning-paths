package com.arcusys.valamis.learningpath.web.servlets.response

import com.arcusys.valamis.learningpath.models.{Goal, GoalAssignment, GoalTypes}
import org.joda.time.{DateTime, Period}

object GoalAssignmentResponse {
  def apply(x: (Goal, GoalAssignment, String)): GoalAssignmentResponse = {
    apply(x._1, x._2, x._3)
  }

  def apply(goal: Goal, goalData: GoalAssignment, title: String): GoalAssignmentResponse = {
    GoalAssignmentResponse(goal.id,
      goal.groupId,
      goal.indexNumber,
      goal.timeLimit,
      goal.optional,
      goal.modifiedDate,
      goalData.assignmentId,
      title
    )
  }
}

case class GoalAssignmentResponse(id: Long,
                              groupId: Option[Long],
                              indexNumber: Int,
                              timeLimit: Option[Period],
                              optional: Boolean,
                              modifiedDate: DateTime,
                              assignmentId: Long,
                              title: String,
                              goalType: GoalTypes.Value = GoalTypes.Assignment) extends GoalResponse

