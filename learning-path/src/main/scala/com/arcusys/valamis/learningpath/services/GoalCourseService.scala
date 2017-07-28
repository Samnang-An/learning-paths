package com.arcusys.valamis.learningpath.services

import com.arcusys.valamis.learningpath.models.{Goal, GoalCourse, GoalStatement}
import org.joda.time.Period

import scala.concurrent.Future

/**
  * Created by mminin on 16/03/2017.
  */
trait GoalCourseService {

  def create(learningPathId: Long,
             timeLimit: Option[Period],
             optional: Boolean,
             courseId: Long)
            (implicit companyId: Long): Future[(Goal, GoalCourse, String)]

  def createInGroup(parentGroupId: Long,
                    timeLimit: Option[Period],
                    optional: Boolean,
                    courseId: Long)
                   (implicit companyId: Long): Future[(Goal, GoalCourse, String)]

  def update(goalId: Long,
             timeLimit: Option[Period],
             optional: Boolean)
            (implicit companyId: Long): Future[(Goal, GoalCourse, String)]
}
