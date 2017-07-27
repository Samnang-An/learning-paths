package com.arcusys.valamis.learningpath.services

import com.arcusys.valamis.learningpath.models.{Goal, GoalLesson, GoalWebContent}
import org.joda.time.Period

import scala.concurrent.Future

trait GoalWebContentService {

  def create(learningPathId: Long,
             timeLimit: Option[Period],
             optional: Boolean,
             webContextId: Long)
            (implicit companyId: Long): Future[(Goal, GoalWebContent, String)]

  def createInGroup(parentGroupId: Long,
                    timeLimit: Option[Period],
                    optional: Boolean,
                    webContextId: Long)
                   (implicit companyId: Long): Future[(Goal, GoalWebContent, String)]

  def update(goalId: Long,
             timeLimit: Option[Period],
             optional: Boolean)
            (implicit companyId: Long): Future[(Goal, GoalWebContent, String)]
}
