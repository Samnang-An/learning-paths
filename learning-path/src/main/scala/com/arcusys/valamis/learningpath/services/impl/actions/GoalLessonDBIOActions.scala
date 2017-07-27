package com.arcusys.valamis.learningpath.services.impl.actions

import com.arcusys.valamis.learningpath.models.{Goal, GoalLesson, GoalStatuses}
import com.arcusys.valamis.learningpath.services.impl.tables.LearningPathTables
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickProfile
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

class GoalLessonDBIOActions(val profile: JdbcProfile)
                           (implicit executionContext: ExecutionContext)
  extends LearningPathTables
    with SlickProfile {

  import profile.api._


  private val selectByGoalIdQ = Compiled { goalId: Rep[Long] =>
    goalLessonTQ.filter(_.goalId === goalId)
  }

  private val getWithGoalInfoByVersionIdQ = Compiled { versionId: Rep[Long] =>
    goalTQ
      .filter(_.versionId === versionId)
      .join(goalLessonTQ).on((goal, group) => group.goalId === goal.id)
  }

  private val getByLessonIdAndNotCompletedUserStatusQ = Compiled { (lessonId: Rep[Long],
                                                                        userId: Rep[Long]) =>
    goalLessonTQ
      .join(userGoalStatusTQ).on((g, s) => g.goalId === s.goalId)
      .filter { case (g, s) =>
        g.lessonId === lessonId &&
          s.userId === userId &&
          (s.status === GoalStatuses.Undefined || s.status === GoalStatuses.InProgress)
      }
      .map { case (g, s) => g }
  }


  def insert(goalLesson: GoalLesson): DBIO[Int] = {
    goalLessonTQ += goalLesson
  }

  def get(goalId: Long): DBIO[Option[GoalLesson]] = {
    selectByGoalIdQ(goalId).result.headOption
  }

  def delete(goalId: Long): DBIO[Int] = {
    selectByGoalIdQ(goalId).delete
  }

  def getWithGoalInfoByVersionId(versionId: Long): DBIO[Seq[(Goal, GoalLesson)]] = {
    getWithGoalInfoByVersionIdQ(versionId).result
  }

  def getByLessonIdAndNotCompletedUserStatus(lessonId: Long, userId: Long): DBIO[Seq[GoalLesson]] = {
    getByLessonIdAndNotCompletedUserStatusQ(lessonId, userId).result
  }
}
