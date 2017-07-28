package com.arcusys.valamis.learningpath.services.impl

import com.arcusys.valamis.learningpath.models.{Goal, GoalAssignment, GoalTypes}
import com.arcusys.valamis.learningpath.services.exceptions.AssignmentIsNotDeployedError
import com.arcusys.valamis.learningpath.services.{AssignmentSupport, GoalAssignmentService, MessageBusService}
import com.arcusys.valamis.learningpath.utils.DbActions
import org.joda.time.Period

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}


class GoalAssignmentServiceImpl(val dbActions: DbActions,
                                val messageBusService: MessageBusService
                               )(implicit val executionContext: ExecutionContext)
  extends GoalAssignmentService
    with GoalServiceBaseImpl {

  import profile.api._

  override val goalType = GoalTypes.Assignment

  override def create(learningPathId: Long,
                      timeLimit: Option[Period],
                      optional: Boolean,
                      assignmentId: Long)
                     (implicit companyId: Long): Future[(Goal, GoalAssignment, String)] = db.run {

    createGoalAction(learningPathId, timeLimit, optional) { goalId =>
      goalAssignmentDBIO.insert(GoalAssignment(goalId, assignmentId))
    } map { goal =>
      (goal, GoalAssignment(goal.id, assignmentId), getAssignmentTitle(assignmentId))
    } transactionally
  }

  override def createInGroup(parentGroupId: Long,
                             timeLimit: Option[Period],
                             optional: Boolean,
                             assignmentId: Long)
                            (implicit companyId: Long): Future[(Goal, GoalAssignment, String)] = db.run {
    createInGroupAction(parentGroupId, timeLimit, optional) { goalId =>
      goalAssignmentDBIO.insert(GoalAssignment(goalId, assignmentId))
    } map { goal =>
      (goal, GoalAssignment(goal.id, assignmentId), getAssignmentTitle(assignmentId))
    } transactionally
  }

  override def update(goalId: Long,
                      timeLimit: Option[Period],
                      optional: Boolean)
                     (implicit companyId: Long): Future[(Goal, GoalAssignment, String)] =
    db.run {
      updateAction(goalId, timeLimit, optional)(goalAssignmentDBIO.get(goalId))() map {
        case (goal, goalData) =>
          (goal, GoalAssignment(goal.id, goalData.assignmentId), getAssignmentTitle(goalData.assignmentId))
      } transactionally
    }

  private def getAssignmentTitle(id: Long): String =
    getAssignmentByIds(Seq(id)) match {
      case Success(items) => items.headOption.map(_.title).getOrElse {
        s"Deleted assignment with id $id"
      }
      case Failure(_: AssignmentIsNotDeployedError) => "Assignments is not deployed"
      case Failure(ex) => throw ex
    }

}
