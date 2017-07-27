package com.arcusys.valamis.learningpath.services.impl.tables

import com.arcusys.valamis.learningpath.models.GoalCourse
import com.arcusys.valamis.learningpath.services.impl.tables.utils.{SlickProfile, TableHelper}

/**
  * Created by mminin on 23/01/2017.
  */
trait GoalCourseTableComponent extends TableHelper {
  self: SlickProfile
    with GoalTableComponent =>

  import profile.api._

  lazy val goalCourseTQ = TableQuery[GoalCourseTable]

  class GoalCourseTable(tag: Tag) extends Table[GoalCourse](tag, tblName("GOAL_COURSE"))
    with GoalLinkSupport {

    val courseId = column[Long]("COURSE_ID")

    def pk = primaryKey("PK_GOAL_COURSE", goalId)

    def * = (goalId, courseId) <> (GoalCourse.tupled, GoalCourse.unapply)
  }

}
