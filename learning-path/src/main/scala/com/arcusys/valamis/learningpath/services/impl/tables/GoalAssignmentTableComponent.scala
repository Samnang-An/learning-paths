package com.arcusys.valamis.learningpath.services.impl.tables

import com.arcusys.valamis.learningpath.models.{GoalAssignment, GoalLesson}
import com.arcusys.valamis.learningpath.services.impl.tables.utils.{SlickProfile, TableHelper}

trait GoalAssignmentTableComponent extends TableHelper {
  self: SlickProfile
    with GoalTableComponent =>

  import profile.api._

  lazy val goalAssignmentTQ = TableQuery[GoalAssignmentTable]

  class GoalAssignmentTable(tag: Tag) extends Table[GoalAssignment](tag, tblName("GOAL_ASSIGNMENT"))
    with GoalLinkSupport {

    val assignmentId = column[Long]("ASSIGNMENT_ID")

    def pk = primaryKey("PK_GOAL_ASSIGNMENT", goalId)

    def * = (goalId, assignmentId) <> (GoalAssignment.tupled, GoalAssignment.unapply)
  }

}
