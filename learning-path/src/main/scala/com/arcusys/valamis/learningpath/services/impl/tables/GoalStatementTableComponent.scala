package com.arcusys.valamis.learningpath.services.impl.tables

import com.arcusys.valamis.learningpath.models.GoalStatement
import com.arcusys.valamis.learningpath.services.impl.tables.utils.{SlickProfile, TableHelper}

trait GoalStatementTableComponent extends TableHelper {
  self: SlickProfile
    with GoalTableComponent =>

  import profile.api._

  lazy val goalStatementTQ = TableQuery[GoalStatementTable]

  class GoalStatementTable(tag: Tag) extends Table[GoalStatement](tag, tblName("GOAL_STATEMENT"))
    with GoalLinkSupport {

    val verbId = column[String]("VERB_ID", O.Length(256, true))
    val objectId = column[String]("OBJECT_ID", O.Length(256, true))
    val objectName = column[String]("OBJECT_NAME", O.Length(512, true))

    def pk = primaryKey("PK_GOAL_STATEMENT", goalId)

    def * = (goalId, verbId, objectId, objectName) <> (GoalStatement.tupled, GoalStatement.unapply)
  }

}
