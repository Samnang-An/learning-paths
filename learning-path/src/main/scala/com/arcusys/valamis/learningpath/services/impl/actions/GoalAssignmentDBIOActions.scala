package com.arcusys.valamis.learningpath.services.impl.actions

import com.arcusys.valamis.learningpath.models.{Goal, GoalAssignment, GoalStatuses}
import com.arcusys.valamis.learningpath.services.impl.tables.LearningPathTables
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickProfile
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

class GoalAssignmentDBIOActions(val profile: JdbcProfile)
                               (implicit executionContext: ExecutionContext)
  extends LearningPathTables
    with SlickProfile {

  import profile.api._


  private val selectByGoalIdQ = Compiled { goalId: Rep[Long] =>
    goalAssignmentTQ.filter(_.goalId === goalId)
  }

  private val getWithGoalInfoByVersionIdQ = Compiled { versionId: Rep[Long] =>
    goalTQ
      .filter(_.versionId === versionId)
      .join(goalAssignmentTQ).on((goal, group) => group.goalId === goal.id)
  }

  private val getByAssignmentIdAndNotCompletedUserStatusQ = Compiled { (assignmentId: Rep[Long],
                                                                        userId: Rep[Long]) =>
    goalAssignmentTQ
      .join(userGoalStatusTQ).on((g, s) => g.goalId === s.goalId)
      .filter { case (g, s) =>
        g.assignmentId === assignmentId &&
          s.userId === userId &&
          (s.status === GoalStatuses.Undefined || s.status === GoalStatuses.InProgress)
      }
      .map { case (g, s) => g }
  }


  def insert(goalAssignment: GoalAssignment): DBIO[Int] = {
    goalAssignmentTQ += goalAssignment
  }

  def get(goalId: Long): DBIO[Option[GoalAssignment]] = {
    selectByGoalIdQ(goalId).result.headOption
  }

  def delete(goalId: Long): DBIO[Int] = {
    selectByGoalIdQ(goalId).delete
  }

  def getWithGoalInfoByVersionId(versionId: Long): DBIO[Seq[(Goal, GoalAssignment)]] = {
    getWithGoalInfoByVersionIdQ(versionId).result
  }

  def getByAssignmentIdAndNotCompletedUserStatus(assignmentId: Long, userId: Long): DBIO[Seq[GoalAssignment]] = {
    getByAssignmentIdAndNotCompletedUserStatusQ(assignmentId, userId).result
  }
}
