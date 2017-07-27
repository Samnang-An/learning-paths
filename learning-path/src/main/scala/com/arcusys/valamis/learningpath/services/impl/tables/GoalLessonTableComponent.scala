package com.arcusys.valamis.learningpath.services.impl.tables

import com.arcusys.valamis.learningpath.models.GoalLesson
import com.arcusys.valamis.learningpath.services.impl.tables.utils.{SlickProfile, TableHelper}

/**
  * Created by mminin on 23/01/2017.
  */
trait GoalLessonTableComponent extends TableHelper {
  self: SlickProfile
    with GoalTableComponent =>

  import profile.api._

  lazy val goalLessonTQ = TableQuery[GoalLessonTable]

  class GoalLessonTable(tag: Tag) extends Table[GoalLesson](tag, tblName("GOAL_LESSON"))
    with GoalLinkSupport {

    val lessonId = column[Long]("LESSON_ID")

    def pk = primaryKey("PK_GOAL_LESSON", goalId)

    def * = (goalId, lessonId) <> (GoalLesson.tupled, GoalLesson.unapply)
  }

}
