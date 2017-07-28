package com.arcusys.valamis.learningpath.services

import com.arcusys.valamis.learningpath.models.{Goal, GoalStatement}
import org.joda.time.Period

import scala.concurrent.Future

/**
  * Created by mminin on 16/03/2017.
  */
trait GoalStatementService {

  def create(learningPathId: Long,
             timeLimit: Option[Period],
             optional: Boolean,
             verbId: String,
             objId: String,
             objectName: String)
            (implicit companyId: Long): Future[(Goal, GoalStatement)]

  def createInGroup(parentGroupId: Long,
                    timeLimit: Option[Period],
                    optional: Boolean,
                    verbId: String,
                    objId: String,
                    objectName: String)
                   (implicit companyId: Long): Future[(Goal, GoalStatement)]

  def update(goalId: Long,
             timeLimit: Option[Period],
             optional: Boolean)
            (implicit companyId: Long): Future[(Goal, GoalStatement)]
}
