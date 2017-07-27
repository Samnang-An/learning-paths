package com.arcusys.valamis.learningpath.tasks

import com.arcusys.valamis.learningpath.models._
import com.arcusys.valamis.learningpath.services.{MessageBusService, TrainingEventServiceBridge}
import com.arcusys.valamis.learningpath.utils.{DbActions, DbActionsSupport}
import com.arcusys.valamis.training.events.model.TrainingEvent
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}

class TrainingEventGoalChecker(val dbActions: DbActions,
                               val trainingEventServiceBridge: TrainingEventServiceBridge,
                               val messageBusService: MessageBusService,
                               val taskManager: TaskManager)
                              (implicit val executionContext: ExecutionContext)
  extends GoalCheckerBase
    with DbActionsSupport {

  override val goalType = GoalTypes.TrainingEvent

  override def updateStatus(goal: Goal,
                            userGoalStatus: UserGoalStatus,
                            now: DateTime)
                           (implicit companyId: Long): Future[Unit] = {
    val endDate = goal.timeLimit.map(userGoalStatus.startedDate.withPeriodAdded(_, 1))

    if (endDate.exists(_ isBefore now)) {
      updateGoalProgress(goal, userGoalStatus, endDate.get, GoalStatuses.Failed)
    } else {
      for {
        trainingEventGoalF <- db.run(goalTrainingEventDBIO.get(goal.id)).map {
          _.getOrElse(throw new Exception("wrong goal type"))
        }

        trainingEventF <- trainingEventServiceBridge.getEvent(trainingEventGoalF.trainingEventId)

        _ <- trainingEventF match {
          case None => updateGoalProgress(goal, userGoalStatus, now, GoalStatuses.InProgress)
          case Some(event) => updateTrainingEventStatus(event, userGoalStatus, goal, now)
        }
      } yield {}
    }
  }

  protected def updateTrainingEventStatus(event: TrainingEvent,
                                        userGoalStatus: UserGoalStatus,
                                        goal: Goal,
                                        now: DateTime)
                                       (implicit companyId: Long) = {
    if (event.manualGoalAchievement) {
      trainingEventServiceBridge.getUserConfirmation(event.id, userGoalStatus.userId) match {
        case Some(confirmation) =>
          if (confirmation.confirmed) {
            updateGoalProgress(goal, userGoalStatus, now, GoalStatuses.Success)
          } else {
            updateGoalProgress(goal, userGoalStatus, now, GoalStatuses.Failed)
          }
        case _ => updateGoalProgress(goal, userGoalStatus, now, GoalStatuses.InProgress)
      }
    } else if (event.endTime.isAfter(now)) {
      updateGoalProgress(goal, userGoalStatus, now, GoalStatuses.InProgress)
    } else if (trainingEventServiceBridge.isUserJoined(event.id, userGoalStatus.userId)) {
      updateGoalProgress(goal, userGoalStatus, now, GoalStatuses.Success)
    } else {
      updateGoalProgress(goal, userGoalStatus, now, GoalStatuses.Failed)
    }
  }
}
