package com.arcusys.valamis.learningpath.services.impl.actions

import com.arcusys.valamis.learningpath.models.{Goal, GoalStatement, GoalStatuses}
import com.arcusys.valamis.learningpath.services.impl.tables.LearningPathTables
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickProfile
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

class GoalStatementDBIOActions(val profile: JdbcProfile)
                              (implicit executionContext: ExecutionContext)
  extends LearningPathTables
    with SlickProfile {

  import profile.api._


  private val selectByGoalIdQ = Compiled { goalId: Rep[Long] =>
    goalStatementTQ.filter(_.goalId === goalId)
  }

  private val getObjectNameByGoalIdQ = Compiled { goalId: Rep[Long] =>
    goalStatementTQ.filter(_.goalId === goalId).map(_.objectName)
  }

  private val getWithGoalInfoByVersionIdQ = Compiled { versionId: Rep[Long] =>
    goalTQ
      .filter(_.versionId === versionId)
      .join(goalStatementTQ).on((goal, group) => group.goalId === goal.id)
  }

  private val getWithGoalInfoByIdQ = Compiled { id: Rep[Long] =>
    goalTQ
      .filter(_.id === id)
      .join(goalStatementTQ).on((goal, group) => group.goalId === goal.id)
  }

  private val getByStatementAndNotCompletedUserStatusQ = Compiled { (verbId: Rep[String],
                                                                     objectId: Rep[String],
                                                                     userId: Rep[Long]) =>
    goalStatementTQ
      .join(userGoalStatusTQ).on((g, s) => g.goalId === s.goalId)
      .filter { case (g, s) =>
        g.objectId === objectId &&
          g.verbId === verbId &&
          s.userId === userId &&
          (s.status === GoalStatuses.Undefined || s.status === GoalStatuses.InProgress)
      }
      .map { case (g, s) => g }
  }


  def insert(goalStatement: GoalStatement): DBIO[Int] = {
    goalStatementTQ += goalStatement
  }

  def updateObjectName(goalId: Long, objectName: String): DBIO[Int] = {
    getObjectNameByGoalIdQ(goalId) update objectName
  }

  def get(goalId: Long): DBIO[Option[GoalStatement]] = {
    selectByGoalIdQ(goalId).result.headOption
  }

  def delete(goalId: Long): DBIO[Int] = {
    selectByGoalIdQ(goalId).delete
  }

  def getWithGoalInfoById(goalId: Long): DBIO[Option[(Goal, GoalStatement)]] = {
    getWithGoalInfoByIdQ(goalId).result.headOption
  }

  def getWithGoalInfoByVersionId(versionId: Long): DBIO[Seq[(Goal, GoalStatement)]] = {
    getWithGoalInfoByVersionIdQ(versionId).result
  }

  def getByStatementAndNotCompletedUserStatus(verbId: String,
                                              objectId: String,
                                              userId: Long): DBIO[Seq[GoalStatement]] = {
    getByStatementAndNotCompletedUserStatusQ(verbId, objectId, userId).result
  }
}
