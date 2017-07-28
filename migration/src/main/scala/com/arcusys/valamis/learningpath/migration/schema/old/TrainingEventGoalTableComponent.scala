package com.arcusys.valamis.learningpath.migration.schema.old

import com.arcusys.valamis.learningpath.migration.schema.old.model.goal.TrainingEventGoal
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickProfile

private[migration] trait TrainingEventGoalTableComponent
  extends CertificateTableComponent
    with CertificateGoalTableComponent { self: SlickProfile =>

  import profile.api._

  class TrainingEventGoalTable(tag: Tag)
    extends Table[TrainingEventGoal](tag, "LEARN_CERT_GOALS_TR_EVENT")
      with CertificateGoalBaseColumns {

    def eventId = column[Long]("EVENT_ID")

    def * = (goalId, certificateId, eventId) <> (TrainingEventGoal.tupled, TrainingEventGoal.unapply)

  }

  val trainingEventGoals = TableQuery[TrainingEventGoalTable]
}