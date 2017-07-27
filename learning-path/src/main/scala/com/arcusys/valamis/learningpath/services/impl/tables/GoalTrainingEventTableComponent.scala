package com.arcusys.valamis.learningpath.services.impl.tables

import com.arcusys.valamis.learningpath.models.{GoalTrainingEvent}
import com.arcusys.valamis.learningpath.services.impl.tables.utils.{SlickProfile, TableHelper}

trait GoalTrainingEventTableComponent extends TableHelper {
  self: SlickProfile
    with GoalTableComponent =>

  import profile.api._

  lazy val goalTrainingEventTQ = TableQuery[GoalTrainingEventTable]

  class GoalTrainingEventTable(tag: Tag) extends Table[GoalTrainingEvent](tag, tblName("GOAL_TRAINING_EVENT"))
    with GoalLinkSupport {

    val trainingEventId = column[Long]("TRAINING_EVENT_ID")

    def pk = primaryKey("PK_TRAINING_EVENT", goalId)

    def * = (goalId, trainingEventId) <> (GoalTrainingEvent.tupled, GoalTrainingEvent.unapply)
  }

}
