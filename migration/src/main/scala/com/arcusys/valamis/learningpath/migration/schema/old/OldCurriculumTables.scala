package com.arcusys.valamis.learningpath.migration.schema.old

import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickProfile

private[migration] trait OldCurriculumTables extends
  CertificateTableComponent

  with CertificateGoalGroupTableComponent
  with CertificateGoalTableComponent

  with ActivityGoalTableComponent
  with AssignmentGoalTableComponent
  with CourseGoalTableComponent
  with PackageGoalTableComponent
  with StatementGoalTableComponent
  with TrainingEventGoalTableComponent

  with CertificateMemberTableComponent

  with CertificateStateTableComponent
  with CertificateGoalStateTableComponent {
  _: SlickProfile =>
}
