package com.arcusys.valamis.learningpath.migration.schema.old

import com.arcusys.valamis.learningpath.migration.schema.old.model.goal.CourseGoal
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickProfile

private[migration] trait CourseGoalTableComponent
  extends LongKeyTableComponent
    with CertificateTableComponent
    with CertificateGoalTableComponent {
  self: SlickProfile =>

  import profile.api._

  class CourseGoalTable(tag: Tag)
    extends Table[CourseGoal](tag, "LEARN_CERT_GOALS_COURSE")
      with CertificateGoalBaseColumns {

    def courseId = column[Long]("COURSE_ID")

    def * = (goalId, certificateId, courseId) <> (CourseGoal.tupled, CourseGoal.unapply)
  }

  val courseGoals = TableQuery[CourseGoalTable]
}