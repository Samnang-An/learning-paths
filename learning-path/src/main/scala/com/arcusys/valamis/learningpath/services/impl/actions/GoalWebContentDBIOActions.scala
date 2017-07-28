package com.arcusys.valamis.learningpath.services.impl.actions

import com.arcusys.valamis.learningpath.models.{Goal, GoalWebContent}
import com.arcusys.valamis.learningpath.services.impl.tables.LearningPathTables
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickProfile
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

class GoalWebContentDBIOActions(val profile: JdbcProfile)
                               (implicit executionContext: ExecutionContext)
  extends LearningPathTables
    with SlickProfile {

  import profile.api._


  private val selectByGoalIdQ = Compiled { goalId: Rep[Long] =>
    goalWebContentTQ.filter(_.goalId === goalId)
  }

  private val selectWithGoalInfoByVersionIdQ = Compiled { versionId: Rep[Long] =>
    goalTQ
      .filter(_.versionId === versionId)
      .join(goalWebContentTQ).on((goal, group) => group.goalId === goal.id)
  }

  private val selectGoalIdsByWebContentIdQ =  Compiled { webContentId: Rep[Long]=>
    goalWebContentTQ.filter(_.webContentId === webContentId)
      .join(goalTQ).on((group, goal) => group.goalId === goal.id)
      .map(_._2.id)
  }

  private val selectWithGoalInfoByIdQ = Compiled { id: Rep[Long] =>
    goalTQ
      .filter(_.id === id)
      .join(goalWebContentTQ).on((goal, group) => group.goalId === goal.id)
  }


  def insert(goalWebContent: GoalWebContent): DBIO[Int] = {
    goalWebContentTQ += goalWebContent
  }

  def get(goalId: Long): DBIO[Option[GoalWebContent]] = {
    selectByGoalIdQ(goalId).result.headOption
  }

  def getGoalIdsByWebContentId(webContentId: Long): DBIO[Seq[Long]] = {
    selectGoalIdsByWebContentIdQ(webContentId).result
  }

  def delete(goalId: Long): DBIO[Int] = {
    selectByGoalIdQ(goalId).delete
  }

  def getWithGoalInfoByVersionId(versionId: Long): DBIO[Seq[(Goal, GoalWebContent)]] = {
    selectWithGoalInfoByVersionIdQ(versionId).result
  }

  def getWithGoalInfoById(goalId: Long): DBIO[Option[(Goal, GoalWebContent)]] = {
    selectWithGoalInfoByIdQ(goalId).result.headOption
  }
}
