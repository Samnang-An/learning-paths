package com.arcusys.valamis.learningpath.services.impl.actions

import com.arcusys.valamis.learningpath.models._
import com.arcusys.valamis.learningpath.services.impl.tables.LearningPathTables
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickProfile
import slick.driver.JdbcProfile

import scala.language.postfixOps

class GoalGroupDBIOActions(val profile: JdbcProfile)
  extends LearningPathTables
    with SlickProfile {

  import profile.api._


  private val selectByGoalIdQ = Compiled { goalId: Rep[Long] =>
    goalGroupTQ.filter(_.goalId === goalId)
  }

  private val selectTitleAndCountByGoalIdQ = Compiled { goalId: Rep[Long] =>
    goalGroupTQ
      .filter(_.goalId === goalId)
      .map(g => (g.title, g.count))
  }

  private val getWithGoalInfoByGoalIdQ = Compiled { goalId: Rep[Long] =>
    val goalQ = goalTQ.filter(_.id === goalId)
    val goalGroupQ = goalGroupTQ.filter(_.goalId === goalId)

    goalQ.join(goalGroupQ).on((goal, group) => group.goalId === goal.id)
  }

  private val getWithGoalInfoByVersionIdQ = Compiled { versionId: Rep[Long] =>
    goalTQ
      .filter(_.versionId === versionId)
      .join(goalGroupTQ).on((goal, group) => group.goalId === goal.id)
  }

  private val getVersionIdByGoalIdQ = Compiled { goalId: Rep[Long] =>
    val goalQ = goalTQ.filter(_.id === goalId)
    val goalGroupQ = goalGroupTQ.filter(_.goalId === goalId)

    goalQ.join(goalGroupQ).on((goal, group) => group.goalId === goal.id)
      .map(_._1.versionId)
  }


  def insert(goalGroup: GoalGroup): DBIO[Int] = {
    goalGroupTQ += goalGroup
  }

  def get(goalId: Long): DBIO[Option[GoalGroup]] = {
    selectByGoalIdQ(goalId).result.headOption
  }

  def getVersionId(goalId: Long): DBIO[Option[Long]] = {
    getVersionIdByGoalIdQ(goalId).result.headOption
  }

  def update(goalId: Long, title: String, count: Option[Int]): DBIO[Int] = {
    selectTitleAndCountByGoalIdQ(goalId) update (title, count)
  }

  def delete(goalId: Long): DBIO[Int] = {
    selectByGoalIdQ(goalId).delete
  }

  def getWithGoalInfo(goalId: Long): DBIO[Option[(Goal, GoalGroup)]] = {
    getWithGoalInfoByGoalIdQ(goalId).result.headOption
  }

  def getWithGoalInfoByVersionId(versionId: Long): DBIO[Seq[(Goal, GoalGroup)]] = {
    getWithGoalInfoByVersionIdQ(versionId).result
  }
}
