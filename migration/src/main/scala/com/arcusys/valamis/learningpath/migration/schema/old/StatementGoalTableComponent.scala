package com.arcusys.valamis.learningpath.migration.schema.old

import com.arcusys.valamis.learningpath.migration.schema.old.model.goal.StatementGoal
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickProfile

private[migration] trait StatementGoalTableComponent
  extends CertificateTableComponent
    with CertificateGoalTableComponent{ self: SlickProfile =>

  import profile.api._

  class StatementGoalTable(tag: Tag)
    extends Table[StatementGoal](tag, "LEARN_CERT_GOALS_STATEMENT")
      with CertificateGoalBaseColumns {

    def verb = column[String]("VERB", O.Length(254, varying = true))
    def obj = column[String]("OBJ", O.Length(254, varying = true))

    def * = (goalId, certificateId, verb, obj) <> (StatementGoal.tupled, StatementGoal.unapply)
  }

  val statementGoals = TableQuery[StatementGoalTable]
}