package com.arcusys.valamis.learningpath.services

import com.arcusys.valamis.learningpath.models.{Goal, GoalLesson}
import org.joda.time.Period

import scala.concurrent.Future

/**
  * Created by mminin on 20/01/2017.
  */
trait GoalLessonService {

  def create(learningPathId: Long,
             timeLimit: Option[Period],
             optional: Boolean,
             lessonId: Long)
            (implicit companyId: Long): Future[(Goal, GoalLesson, String)]

  def createInGroup(parentGroupId: Long,
                    timeLimit: Option[Period],
                    optional: Boolean,
                    lessonId: Long)
                   (implicit companyId: Long): Future[(Goal, GoalLesson, String)]

  def update(goalId: Long,
             timeLimit: Option[Period],
             optional: Boolean)
            (implicit companyId: Long): Future[(Goal, GoalLesson, String)]
}
