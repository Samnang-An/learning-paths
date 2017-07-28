package com.arcusys.valamis.learningpath.services.impl.actions

import com.arcusys.valamis.learningpath.models.{Goal, GoalStatuses, GoalTrainingEvent}
import com.arcusys.valamis.learningpath.services.impl.tables.LearningPathTables
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickProfile
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

class GoalTrainingEventDBIOActions(val profile: JdbcProfile)
                                  (implicit executionContext: ExecutionContext)
  extends LearningPathTables
    with SlickProfile {

  import profile.api._


  private val selectByGoalIdQ = Compiled { goalId: Rep[Long] =>
    goalTrainingEventTQ.filter(_.goalId === goalId)
  }

  private val getWithGoalInfoByVersionIdQ = Compiled { versionId: Rep[Long] =>
    goalTQ
      .filter(_.versionId === versionId)
      .join(goalTrainingEventTQ).on((goal, group) => group.goalId === goal.id)
  }

  private val getByTrainingEventIdAndNotCompletedUserStatusQ = Compiled { (eventId: Rep[Long],
                                                                        userId: Rep[Long]) =>
    goalTrainingEventTQ
      .join(userGoalStatusTQ).on((g, s) => g.goalId === s.goalId)
      .filter { case (g, s) =>
        g.trainingEventId === eventId &&
          s.userId === userId &&
          (s.status === GoalStatuses.Undefined || s.status === GoalStatuses.InProgress)
      }
      .map { case (g, s) => g }
  }

  def insert(goalTrainingEvent: GoalTrainingEvent): DBIO[Int] = {
    goalTrainingEventTQ += goalTrainingEvent
  }

  def get(goalId: Long): DBIO[Option[GoalTrainingEvent]] = {
    selectByGoalIdQ(goalId).result.headOption
  }

  def delete(goalId: Long): DBIO[Int] = {
    selectByGoalIdQ(goalId).delete
  }

  def getWithGoalInfoByVersionId(versionId: Long): DBIO[Seq[(Goal, GoalTrainingEvent)]] = {
    getWithGoalInfoByVersionIdQ(versionId).result
  }

  def getByTrainingEventIdAndNotCompletedUserStatus(eventId: Long, userId: Long): DBIO[Seq[GoalTrainingEvent]] = {
    getByTrainingEventIdAndNotCompletedUserStatusQ(eventId, userId).result
  }
}
