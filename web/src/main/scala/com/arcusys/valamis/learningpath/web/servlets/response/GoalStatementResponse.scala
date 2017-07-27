package com.arcusys.valamis.learningpath.web.servlets.response

import com.arcusys.valamis.learningpath.models.{Goal, GoalStatement, GoalTypes}
import org.joda.time.{DateTime, Period}

/**
  * Created by mminin on 16/03/2017.
  */
object GoalStatementResponse {
  def apply(x: (Goal, GoalStatement)): GoalStatementResponse = {
    apply(x._1, x._2)
  }

  def apply(goal: Goal, goalData: GoalStatement): GoalStatementResponse = {
    new GoalStatementResponse(
      goal.id,
      goal.groupId,
      goal.indexNumber,
      goal.timeLimit,
      goal.optional,
      goal.modifiedDate,
      goalData.verbId,
      goalData.objectId,
      goalData.objectName
    )
  }
}

case class GoalStatementResponse(id: Long,
                                 groupId: Option[Long],
                                 indexNumber: Int,
                                 timeLimit: Option[Period],
                                 optional: Boolean,
                                 modifiedDate: DateTime,
                                 verbId: String,
                                 objectId: String,
                                 objectName: String,
                                 goalType: GoalTypes.Value = GoalTypes.Statement
                                ) extends GoalResponse