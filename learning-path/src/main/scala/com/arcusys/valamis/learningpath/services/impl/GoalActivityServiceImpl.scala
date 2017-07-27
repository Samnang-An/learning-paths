package com.arcusys.valamis.learningpath.services.impl

import com.arcusys.valamis.learningpath.models.{Goal, GoalLRActivity, GoalTypes}
import com.arcusys.valamis.learningpath.services.GoalActivityService
import com.arcusys.valamis.learningpath.utils.DbActions
import org.joda.time.Period

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

/**
  * Created by mminin on 26/01/2017.
  */
class GoalActivityServiceImpl(val dbActions: DbActions)
                             (implicit val executionContext: ExecutionContext)
  extends GoalActivityService
    with GoalServiceBaseImpl {

  import profile.api._

  override val goalType = GoalTypes.LRActivity

  override def create(learningPathId: Long,
                      timeLimit: Option[Period],
                      optional: Boolean,
                      activityName: String,
                      count: Int)
                     (implicit companyId: Long): Future[(Goal, GoalLRActivity)] = db.run {
    createGoalAction(learningPathId, timeLimit, optional) { goalId =>
      goalActivityDBIO.insert(GoalLRActivity(goalId, activityName, count))
    } map { goal =>
      (goal, GoalLRActivity(goal.id, activityName, count))
    } transactionally
  }

  override def createInGroup(parentGroupId: Long,
                             timeLimit: Option[Period],
                             optional: Boolean,
                             activityName: String,
                             count: Int)
                            (implicit companyId: Long): Future[(Goal, GoalLRActivity)] = db.run {
    createInGroupAction(parentGroupId, timeLimit, optional) {
      goalId => goalActivityDBIO.insert(GoalLRActivity(goalId, activityName, count))
    } map { goal =>
      (goal, GoalLRActivity(goal.id, activityName, count))
    } transactionally
  }

  def update(goalId: Long,
             timeLimit: Option[Period],
             optional: Boolean,
             count: Int)
            (implicit companyId: Long): Future[(Goal, GoalLRActivity)] = db.run {
    updateAction(goalId, timeLimit, optional)(goalActivityDBIO.get(goalId)) {
      goalActivityDBIO.updateCount(goalId, count)
    } map { case (goal, goalData) =>
      (goal, GoalLRActivity(goalId, goalData.activityName, count))
    } transactionally
  }
}
