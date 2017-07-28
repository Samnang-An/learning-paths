package com.arcusys.valamis.learningpath.web.servlets.response

import com.arcusys.valamis.learningpath.models.{Goal, GoalLesson, GoalTypes}
import org.joda.time.{DateTime, Period}

/**
  * Created by mminin on 20/02/2017.
  */
object GoalLessonResponse {
  def apply(x: (Goal, GoalLesson, String)): GoalLessonResponse = {
    apply(x._1, x._2, x._3)
  }

  def apply(goal: Goal, goalData: GoalLesson, title: String): GoalLessonResponse = {
    GoalLessonResponse(goal.id,
      goal.groupId,
      goal.indexNumber,
      goal.timeLimit,
      goal.optional,
      goal.modifiedDate,
      goalData.lessonId,
      title
    )
  }
}

case class GoalLessonResponse(id: Long,
                              groupId: Option[Long],
                              indexNumber: Int,
                              timeLimit: Option[Period],
                              optional: Boolean,
                              modifiedDate: DateTime,
                              lessonId: Long,
                              title: String,
                              goalType: GoalTypes.Value = GoalTypes.Lesson) extends GoalResponse

