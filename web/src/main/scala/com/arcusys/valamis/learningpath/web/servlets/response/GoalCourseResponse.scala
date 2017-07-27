package com.arcusys.valamis.learningpath.web.servlets.response

import com.arcusys.valamis.learningpath.models.{Goal, GoalCourse, GoalTypes}
import org.joda.time.{DateTime, Period}

object GoalCourseResponse {
  def apply(x: (Goal, GoalCourse, String)): GoalCourseResponse = {
    apply(x._1, x._2, x._3)
  }

  def apply(goal: Goal, goalData: GoalCourse, title: String): GoalCourseResponse = {
    GoalCourseResponse(goal.id,
      goal.groupId,
      goal.indexNumber,
      goal.timeLimit,
      goal.optional,
      goal.modifiedDate,
      goalData.courseId,
      title
    )
  }
}

case class GoalCourseResponse(id: Long,
                                  groupId: Option[Long],
                                  indexNumber: Int,
                                  timeLimit: Option[Period],
                                  optional: Boolean,
                                  modifiedDate: DateTime,
                                  courseId: Long,
                                  title: String,
                                  goalType: GoalTypes.Value = GoalTypes.Course) extends GoalResponse

