package com.arcusys.valamis.learningpath.migration.schema.old

import com.arcusys.valamis.learningpath.migration.schema.old.model.goal.ActivityGoal
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickProfile

private[migration] trait ActivityGoalTableComponent
  extends CertificateTableComponent
    with CertificateGoalTableComponent { self: SlickProfile =>

  import profile.api._

  class ActivityGoalTable(tag: Tag)
    extends Table[ActivityGoal](tag, "LEARN_CERT_GOALS_ACTIVITY")
      with CertificateGoalBaseColumns {

    def activityName = column[String]("ACTIVITY_NAME", O.Length(254, varying = true))
    def count = column[Int]("COUNT")

    def * = (goalId, certificateId, activityName, count) <> (ActivityGoal.tupled, ActivityGoal.unapply)

  }

  val activityGoals = TableQuery[ActivityGoalTable]
}