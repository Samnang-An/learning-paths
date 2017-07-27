package com.arcusys.valamis.learningpath.services.impl.actions

import com.arcusys.valamis.learningpath.models.{Goal,GoalCourse, GoalStatuses}
import com.arcusys.valamis.learningpath.services.impl.tables.LearningPathTables
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickProfile
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

class GoalCourseDBIOActions(val profile: JdbcProfile)
                           (implicit executionContext: ExecutionContext)
  extends LearningPathTables
    with SlickProfile {

  import profile.api._


  private val selectByGoalIdQ = Compiled { goalId: Rep[Long] =>
    goalCourseTQ.filter(_.goalId === goalId)
  }

  private val getWithGoalInfoByVersionIdQ = Compiled { versionId: Rep[Long] =>
    goalTQ
      .filter(_.versionId === versionId)
      .join(goalCourseTQ).on((goal, group) => group.goalId === goal.id)
  }

  private val getByCourseIdAndNotCompletedUserStatusQ = Compiled { (courseId: Rep[Long],
                                                                        userId: Rep[Long]) =>
    goalCourseTQ
      .join(userGoalStatusTQ).on((g, s) => g.goalId === s.goalId)
      .filter { case (g, s) =>
        g.courseId === courseId &&
          s.userId === userId &&
          (s.status === GoalStatuses.Undefined || s.status === GoalStatuses.InProgress)
      }
      .map { case (g, s) => g }
  }


  def insert(goalCourse: GoalCourse): DBIO[Int] = {
    goalCourseTQ += goalCourse
  }

  def get(goalId: Long): DBIO[Option[GoalCourse]] = {
    selectByGoalIdQ(goalId).result.headOption
  }

  def delete(goalId: Long): DBIO[Int] = {
    selectByGoalIdQ(goalId).delete
  }

  def getWithGoalInfoByVersionId(versionId: Long): DBIO[Seq[(Goal, GoalCourse)]] = {
    getWithGoalInfoByVersionIdQ(versionId).result
  }

  def getByCourseIdAndNotCompletedUserStatus(courseId: Long, userId: Long): DBIO[Seq[GoalCourse]] = {
    getByCourseIdAndNotCompletedUserStatusQ(courseId, userId).result
  }
}
