package com.arcusys.valamis.learningpath.services

import com.arcusys.valamis.learningpath.models.{Goal, GoalAssignment}
import org.joda.time.Period

import scala.concurrent.Future

trait GoalAssignmentService
  extends AssignmentSupport {

  def create(learningPathId: Long,
             timeLimit: Option[Period],
             optional: Boolean,
             assignmentId: Long)
            (implicit companyId: Long): Future[(Goal, GoalAssignment, String)]

  def createInGroup(parentGroupId: Long,
                    timeLimit: Option[Period],
                    optional: Boolean,
                    assignmentId: Long)
                   (implicit companyId: Long): Future[(Goal, GoalAssignment, String)]

  def update(goalId: Long,
             timeLimit: Option[Period],
             optional: Boolean)
            (implicit companyId: Long): Future[(Goal, GoalAssignment, String)]
}
