package com.arcusys.valamis.learningpath.services.impl.actions

import com.arcusys.valamis.learningpath.models.{Goal, GoalLRActivity, GoalStatuses}
import com.arcusys.valamis.learningpath.services.impl.tables.LearningPathTables
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickProfile
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

class GoalLRActivityDBIOActions(val profile: JdbcProfile)
                               (implicit executionContext: ExecutionContext)
  extends LearningPathTables
    with SlickProfile {

  import profile.api._


  private val selectByGoalIdQ = Compiled { goalId: Rep[Long] =>
    goalLRActivityTQ.filter(_.goalId === goalId)
  }

  private val selectCountByGoalIdQ = Compiled { goalId: Rep[Long] =>
    goalLRActivityTQ
      .filter(_.goalId === goalId)
      .map(_.count)
  }

  private val getWithGoalInfoByVersionIdQ = Compiled { versionId: Rep[Long] =>
    goalTQ
      .filter(_.versionId === versionId)
      .join(goalLRActivityTQ).on((goal, group) => group.goalId === goal.id)
  }

  private val getByActivityNameAndNotCompletedUserStatusQ = Compiled { (activityName: Rep[String],
                                                                      userId: Rep[Long]) =>
    goalLRActivityTQ
      .join(userGoalStatusTQ).on((g, s) => g.goalId === s.goalId)
      .filter { case (g, s) =>
        g.activityName === activityName &&
          s.userId === userId &&
          (s.status === GoalStatuses.Undefined || s.status === GoalStatuses.InProgress)
      }
      .map { case (g, s) => g }
  }

  def insert(goalActivity: GoalLRActivity): DBIO[Int] = {
    goalLRActivityTQ += goalActivity
  }

  def updateCount(goalId: Long, count: Int): DBIO[Int] = {
    selectCountByGoalIdQ(goalId) update count
  }

  def get(goalId: Long): DBIO[Option[GoalLRActivity]] = {
    selectByGoalIdQ(goalId).result.headOption
  }

  def delete(goalId: Long): DBIO[Int] = {
    selectByGoalIdQ(goalId).delete
  }

  def getWithGoalInfoByVersionId(versionId: Long): DBIO[Seq[(Goal, GoalLRActivity)]] = {
    getWithGoalInfoByVersionIdQ(versionId).result
  }

  def getByActivityNameAndNotCompletedUserStatus(activityName: String,
                                                 userId: Long
                                                ): DBIO[Seq[GoalLRActivity]] = {
    getByActivityNameAndNotCompletedUserStatusQ(activityName, userId).result
  }
}
