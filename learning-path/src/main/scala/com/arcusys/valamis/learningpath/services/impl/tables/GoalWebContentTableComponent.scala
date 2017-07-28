package com.arcusys.valamis.learningpath.services.impl.tables

import com.arcusys.valamis.learningpath.models.GoalWebContent
import com.arcusys.valamis.learningpath.services.impl.tables.utils.{SlickProfile, TableHelper}

trait GoalWebContentTableComponent extends TableHelper {
  self: SlickProfile
    with GoalTableComponent =>

  import profile.api._

  lazy val goalWebContentTQ = TableQuery[GoalWebContentTable]

  class GoalWebContentTable(tag: Tag) extends Table[GoalWebContent](tag, tblName("GOAL_WEBCONTENT"))
    with GoalLinkSupport {

    val webContentId = column[Long]("WEBCONTENT_ID")

    def pk = primaryKey("PK_WEBCONTENT", goalId)

    def * = (goalId, webContentId) <> (GoalWebContent.tupled, GoalWebContent.unapply)
  }

}
