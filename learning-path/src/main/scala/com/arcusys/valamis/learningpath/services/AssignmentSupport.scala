package com.arcusys.valamis.learningpath.services

import com.arcusys.valamis.learningpath.models.{Assignment, AssignmentMessageActionType, AssignmentMessageFields, UserStatuses}
import com.arcusys.valamis.learningpath.serializer.AssignmentSerializer
import com.arcusys.valamis.learningpath.services.exceptions.AssignmentIsNotDeployedError

import scala.util.{Success, Try}

/**
  * Created by pkornilov on 3/10/17.
  */
trait AssignmentSupport extends MessageBusSupport
  with AssignmentSerializer {

  protected val destination = "valamis/assignment"
  override protected def notDeployedError: Throwable = new AssignmentIsNotDeployedError

  def isAssignmentDeployed: Boolean = {
    val messageValues = prepareMessageData(Map("action" -> AssignmentMessageActionType.Check.toString))

    messageBusService.sendSynchronousMessage(destination, messageValues).toOption.contains("deployed")
  }

  def getSubmissionStatus(assignmentId: Long, userId: Long): Try[UserStatuses.Value] = {
    sendMessage(Map(
      AssignmentMessageFields.Action -> AssignmentMessageActionType.SubmissionStatus.toString,
      AssignmentMessageFields.AssignmentId -> assignmentId.toString,
      AssignmentMessageFields.UserId -> userId.toString
    )) {
      UserStatuses.withName
    }
  }

  def getAssignmentByIds(assignmentIds: Seq[Long]): Try[Seq[Assignment]] = {
    assignmentIds match {
      case Seq() => Success(Seq())
      case ids =>
        sendMessage(Map(
          AssignmentMessageFields.Action -> AssignmentMessageActionType.ByIds.toString,
          AssignmentMessageFields.AssignmentIds -> ids.mkString(",")
        )) {
          deserializeAssignments
        }
    }

  }
}
