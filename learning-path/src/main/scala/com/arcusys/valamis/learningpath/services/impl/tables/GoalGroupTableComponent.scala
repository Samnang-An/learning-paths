package com.arcusys.valamis.learningpath.services.impl.tables

import com.arcusys.valamis.learningpath.models.GoalGroup
import com.arcusys.valamis.learningpath.services.impl.tables.utils.{SlickProfile, TableHelper}

/**
  * Created by mminin on 23/01/2017.
  */
trait GoalGroupTableComponent extends TableHelper {
  self: SlickProfile
    with GoalTableComponent =>

  import profile.api._

  lazy val goalGroupTQ = TableQuery[GoalGroupTable]

  class GoalGroupTable(tag: Tag) extends Table[GoalGroup](tag, tblName("GOAL_GROUP"))
    with GoalLinkSupport {

    val title = column[String]("TITLE", O.Length(titleSize, varying = true))
    val count = column[Option[Int]]("COUNT")

    def pk = primaryKey("PK_GOAL_GROUP", goalId)

    def * = (goalId, title, count) <> (GoalGroup.tupled, GoalGroup.unapply)
  }

}
