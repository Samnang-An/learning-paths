package com.arcusys.valamis.learningpath.services.impl

import com.arcusys.valamis.learningpath.models.{Goal, GoalStatement, GoalTypes}
import com.arcusys.valamis.learningpath.services.GoalStatementService
import com.arcusys.valamis.learningpath.utils.DbActions
import org.joda.time.Period

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

/**
  * Created by mminin on 16/03/2017.
  */
class GoalStatementServiceImpl(val dbActions: DbActions)
                              (implicit val executionContext: ExecutionContext)
  extends GoalStatementService
    with GoalServiceBaseImpl {

  import profile.api._

  override def goalType = GoalTypes.Statement

  def create(learningPathId: Long,
             timeLimit: Option[Period],
             optional: Boolean,
             verbId: String,
             objectId: String,
             objectName: String)
            (implicit companyId: Long): Future[(Goal, GoalStatement)] = db.run {

    createGoalAction(learningPathId, timeLimit, optional) { goalId =>
      goalStatementDBIO.insert(GoalStatement(goalId, verbId, objectId, objectName))
    } map { goal =>
      (goal, GoalStatement(goal.id, verbId, objectId, objectName))
    } transactionally
  }


  def createInGroup(parentGroupId: Long,
                    timeLimit: Option[Period],
                    optional: Boolean,
                    verbId: String,
                    objectId: String,
                    objectName: String)
                   (implicit companyId: Long): Future[(Goal, GoalStatement)] = db.run {

    createInGroupAction(parentGroupId, timeLimit, optional) { goalId =>
      goalStatementDBIO.insert(GoalStatement(goalId, verbId, objectId, objectName))
    } map { goal =>
      (goal, GoalStatement(goal.id, verbId, objectId, objectName))
    } transactionally
  }

  def update(goalId: Long,
             timeLimit: Option[Period],
             optional: Boolean)
            (implicit companyId: Long): Future[(Goal, GoalStatement)] = db.run {

    updateAction(goalId, timeLimit, optional)(goalStatementDBIO.get(goalId))()
      .transactionally
  }
}
