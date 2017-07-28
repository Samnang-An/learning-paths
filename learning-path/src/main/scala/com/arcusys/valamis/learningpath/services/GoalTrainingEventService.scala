package com.arcusys.valamis.learningpath.services

import com.arcusys.valamis.learningpath.models.{Goal, GoalTrainingEvent}
import org.joda.time.Period

import scala.concurrent.Future

trait GoalTrainingEventService {

  def create(learningPathId: Long,
             timeLimit: Option[Period],
             optional: Boolean,
             trainingEventId: Long)
            (implicit companyId: Long): Future[(Goal, GoalTrainingEvent, String)]

  def createInGroup(parentGroupId: Long,
                    timeLimit: Option[Period],
                    optional: Boolean,
                    trainingEventId: Long)
                   (implicit companyId: Long): Future[(Goal, GoalTrainingEvent, String)]

  def update(goalId: Long,
             timeLimit: Option[Period],
             optional: Boolean)
            (implicit companyId: Long): Future[(Goal, GoalTrainingEvent, String)]
}
