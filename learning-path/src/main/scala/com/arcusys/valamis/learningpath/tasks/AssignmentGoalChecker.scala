package com.arcusys.valamis.learningpath.tasks

import com.arcusys.valamis.learningpath.models._
import com.arcusys.valamis.learningpath.services.exceptions.AssignmentIsNotDeployedError
import com.arcusys.valamis.learningpath.services.{AssignmentSupport, MessageBusService}
import com.arcusys.valamis.learningpath.utils.{DbActions, DbActionsSupport}
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}

class AssignmentGoalChecker(val dbActions: DbActions,
                            val messageBusService: MessageBusService,
                            val taskManager: TaskManager)
                           (implicit val executionContext: ExecutionContext)
  extends GoalCheckerBase
    with AssignmentSupport
    with DbActionsSupport {

  override val goalType = GoalTypes.Assignment

  override def updateStatus(goal: Goal,
                            userGoalStatus: UserGoalStatus,
                            now: DateTime)
                           (implicit companyId: Long): Future[Unit] = {
    val endDate = goal.timeLimit.map(userGoalStatus.startedDate.withPeriodAdded(_, 1))


    if (endDate.exists(_ isBefore now)) {
      //TODO take into account assignment pass date
      updateGoalProgress(goal, userGoalStatus, endDate.get, GoalStatuses.Failed)
    } else {
      val checkFuture =
        for {
          assignmentGoal <- db.run(goalAssignmentDBIO.get(goal.id)).map {
            _.getOrElse(throw new Exception("wrong goal type"))
          }

          assignment <- Future.fromTry(getAssignmentByIds(Seq(assignmentGoal.assignmentId)).map(_.headOption))

          _ <- assignment match {
            case None =>
              updateGoalProgress(goal, userGoalStatus, now, GoalStatuses.InProgress)
            case Some(_) =>
              Future.fromTry(getSubmissionStatus(assignmentGoal.assignmentId, userGoalStatus.userId)) flatMap {
                case UserStatuses.Completed =>
                  updateGoalProgress(goal, userGoalStatus, now, GoalStatuses.Success)
                case _ =>
                  updateGoalProgress(goal, userGoalStatus, now, GoalStatuses.InProgress)
              }
          }

        } yield {}

      checkFuture recoverWith {
        case _: AssignmentIsNotDeployedError =>
          updateGoalProgress(goal, userGoalStatus, now, GoalStatuses.InProgress)
      }

    }
  }


}
