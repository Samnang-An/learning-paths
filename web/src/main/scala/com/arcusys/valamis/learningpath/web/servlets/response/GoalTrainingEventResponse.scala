package com.arcusys.valamis.learningpath.web.servlets.response

import com.arcusys.valamis.learningpath.models.{Goal, GoalLesson, GoalTrainingEvent, GoalTypes}
import org.joda.time.{DateTime, Period}

object GoalTrainingEventResponse {
  def apply(x: (Goal, GoalTrainingEvent, String)): GoalTrainingEventResponse = {
    apply(x._1, x._2, x._3)
  }

  def apply(goal: Goal, goalData: GoalTrainingEvent, title: String): GoalTrainingEventResponse = {
    GoalTrainingEventResponse(goal.id,
      goal.groupId,
      goal.indexNumber,
      goal.timeLimit,
      goal.optional,
      goal.modifiedDate,
      goalData.trainingEventId,
      title
    )
  }
}

case class GoalTrainingEventResponse(id: Long,
                              groupId: Option[Long],
                              indexNumber: Int,
                              timeLimit: Option[Period],
                              optional: Boolean,
                              modifiedDate: DateTime,
                              trainingEventId: Long,
                              title: String,
                              goalType: GoalTypes.Value = GoalTypes.TrainingEvent) extends GoalResponse
