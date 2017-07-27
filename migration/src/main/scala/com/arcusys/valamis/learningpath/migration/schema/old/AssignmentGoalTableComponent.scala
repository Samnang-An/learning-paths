package com.arcusys.valamis.learningpath.migration.schema.old

import com.arcusys.valamis.learningpath.migration.schema.old.model.goal.AssignmentGoal
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickProfile

private[migration] trait AssignmentGoalTableComponent
  extends CertificateTableComponent
    with CertificateGoalTableComponent {
  self: SlickProfile =>

  import profile.api._

  class AssignmentGoalTable(tag: Tag)
    extends Table[AssignmentGoal](tag, "LEARN_CERT_GOALS_ASSIGNMENT")
      with CertificateGoalBaseColumns {

    def assignmentId = column[Long]("ASSIGNMENT_ID")

    def * = (goalId, certificateId, assignmentId) <> (AssignmentGoal.tupled, AssignmentGoal.unapply)

  }

  val assignmentGoals = TableQuery[AssignmentGoalTable]
}