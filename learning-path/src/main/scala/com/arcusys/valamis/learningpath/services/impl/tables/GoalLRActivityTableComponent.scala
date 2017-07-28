package com.arcusys.valamis.learningpath.services.impl.tables

import com.arcusys.valamis.learningpath.models.GoalLRActivity
import com.arcusys.valamis.learningpath.services.impl.tables.utils.{SlickProfile, TableHelper}

/**
  * Created by mminin on 23/01/2017.
  */
trait GoalLRActivityTableComponent extends TableHelper {
  self: SlickProfile
    with GoalTableComponent =>

  import profile.api._

  lazy val goalLRActivityTQ = TableQuery[GoalActivityTable]

  class GoalActivityTable(tag: Tag) extends Table[GoalLRActivity](tag, tblName("GOAL_ACTIVITY"))
    with GoalLinkSupport {

    val activityName = column[String]("ACTIVITY_NAME", O.Length(256, varying = true))
    val count = column[Int]("COUNT")

    def pk = primaryKey("PK_GOAL_ACTIVITY", goalId)

    def * = (goalId, activityName, count) <> (GoalLRActivity.tupled, GoalLRActivity.unapply)
  }

}
