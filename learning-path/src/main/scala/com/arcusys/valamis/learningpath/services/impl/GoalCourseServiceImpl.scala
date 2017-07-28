package com.arcusys.valamis.learningpath.services.impl

import com.arcusys.valamis.learningpath.models.{Goal, GoalCourse, GoalTypes}
import com.arcusys.valamis.learningpath.services.{CourseService, GoalCourseService}
import com.arcusys.valamis.learningpath.utils.DbActions
import org.joda.time.Period

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps


class GoalCourseServiceImpl(val dbActions: DbActions,
                            courseService: CourseService)
                           (implicit val executionContext: ExecutionContext)
  extends GoalCourseService
    with GoalServiceBaseImpl {

  import profile.api._

  override def goalType = GoalTypes.Course

  def create(learningPathId: Long,
             timeLimit: Option[Period],
             optional: Boolean,
             courseId: Long)
            (implicit companyId: Long): Future[(Goal, GoalCourse, String)] = db.run {

    createGoalAction(learningPathId, timeLimit, optional) { goalId =>
      goalCourseDBIO.insert(GoalCourse(goalId, courseId))
    } map { goal =>
      (goal, GoalCourse(goal.id, courseId))
    } transactionally
  } flatMap toFullInfo


  def createInGroup(parentGroupId: Long,
                    timeLimit: Option[Period],
                    optional: Boolean,
                    courseId: Long)
                   (implicit companyId: Long): Future[(Goal, GoalCourse, String)] = db.run {

    createInGroupAction(parentGroupId, timeLimit, optional) { goalId =>
      goalCourseDBIO.insert(GoalCourse(goalId, courseId))
    } map { goal =>
      (goal, GoalCourse(goal.id, courseId))
    } transactionally
  } flatMap toFullInfo

  def update(goalId: Long,
             timeLimit: Option[Period],
             optional: Boolean)
            (implicit companyId: Long): Future[(Goal, GoalCourse, String)] = db.run {

    updateAction(goalId, timeLimit, optional)(goalCourseDBIO.get(goalId))()
      .transactionally
  } flatMap toFullInfo

  private def toFullInfo(g: (Goal, GoalCourse)) = {
    val (goal, goalCourse) = g
    val courseId = goalCourse.courseId
    courseService.getCourseTitlesByIds(Seq(courseId)).map { items =>
      (goal, goalCourse, items.getOrElse(courseId, "Deleted course with id " + courseId))
    }
  }
}
