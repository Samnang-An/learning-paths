package com.arcusys.valamis.learningpath.services.impl

import com.arcusys.valamis.learningpath.models.{Goal, GoalTrainingEvent, GoalTypes}
import com.arcusys.valamis.learningpath.services.{GoalTrainingEventService, TrainingEventServiceBridge}
import com.arcusys.valamis.learningpath.utils.DbActions
import org.joda.time.Period

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps


class GoalTrainingEventServiceImpl(val dbActions: DbActions,
                                   trainingEventServiceBridge: TrainingEventServiceBridge)
                                  (implicit val executionContext: ExecutionContext)
  extends GoalTrainingEventService
    with GoalServiceBaseImpl {

  import profile.api._

  override val goalType = GoalTypes.TrainingEvent

  override def create(learningPathId: Long,
                      timeLimit: Option[Period],
                      optional: Boolean,
                      trainingEventId: Long)
                     (implicit companyId: Long): Future[(Goal, GoalTrainingEvent, String)] = db.run {

    createGoalAction(learningPathId, timeLimit, optional) { goalId =>
      goalTrainingEventDBIO.insert(GoalTrainingEvent(goalId, trainingEventId))
    } map { goal =>
      (goal, GoalTrainingEvent(goal.id, trainingEventId))
    } transactionally
  } flatMap toFullInfo

  override def createInGroup(parentGroupId: Long,
                             timeLimit: Option[Period],
                             optional: Boolean,
                             trainingEventId: Long)
                            (implicit companyId: Long): Future[(Goal, GoalTrainingEvent, String)] = db.run {
    createInGroupAction(parentGroupId, timeLimit, optional) { goalId =>
      goalTrainingEventDBIO.insert(GoalTrainingEvent(goalId, trainingEventId))
    } map { goal =>
      (goal, GoalTrainingEvent(goal.id, trainingEventId))
    } transactionally
  } flatMap toFullInfo

  override def update(goalId: Long,
                      timeLimit: Option[Period],
                      optional: Boolean)
                     (implicit companyId: Long): Future[(Goal, GoalTrainingEvent, String)] = db.run {
    updateAction(goalId, timeLimit, optional)(goalTrainingEventDBIO.get(goalId))().transactionally
  } flatMap toFullInfo


  private def toFullInfo(g: (Goal, GoalTrainingEvent)) = {
    trainingEventServiceBridge.getEventTitle(g._2.trainingEventId)
      .map(title => (g._1, g._2, title.getOrElse("Deleted event with id " + g._2.trainingEventId)))
  }
}