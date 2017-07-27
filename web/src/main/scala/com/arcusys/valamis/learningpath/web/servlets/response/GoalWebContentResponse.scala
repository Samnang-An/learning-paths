package com.arcusys.valamis.learningpath.web.servlets.response

import com.arcusys.valamis.learningpath.models._
import org.joda.time.{DateTime, Period}


object GoalWebContentResponse {
  def apply(x: (Goal, GoalWebContent, String)): GoalWebContentResponse = {
    apply(x._1, x._2, x._3)
  }

  def apply(goal: Goal, goalData: GoalWebContent, title: String): GoalWebContentResponse = {
    GoalWebContentResponse(goal.id,
      goal.groupId,
      goal.indexNumber,
      goal.timeLimit,
      goal.optional,
      goal.modifiedDate,
      goalData.webContentId,
      title
    )
  }
}

case class GoalWebContentResponse(id: Long,
                              groupId: Option[Long],
                              indexNumber: Int,
                              timeLimit: Option[Period],
                              optional: Boolean,
                              modifiedDate: DateTime,
                              webContentId: Long,
                              title: String,
                              goalType: GoalTypes.Value = GoalTypes.WebContent) extends GoalResponse

