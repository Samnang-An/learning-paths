package com.arcusys.valamis.learningpath

import com.arcusys.valamis.learningpath.services.TrainingEventServiceBridge
import com.arcusys.valamis.training.events.model.{TrainingEvent, TrainingEventMemberConfirmation}
import com.arcusys.valamis.training.events.service.TrainingService

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

// TODO Improve this test implementation
class TrainingEventServiceBridgeTestImpl(trainingEvents: Seq[TrainingEvent] = Nil)
                                        (implicit executionContext: ExecutionContext)
  extends TrainingEventServiceBridge {

  override def isTrainingEventsDeployed(companyId: Long): Boolean = true

  override def trainingService: TrainingService = ???

  override def getEventTitle(eventId: Long): Future[Option[String]] = {
    Future(Some("event"))
  }

  override def getEvent(eventId: Long): Future[Option[TrainingEvent]] = {
    Future(None)
  }

  override def isUserJoined(eventId: Long, userId: Long): Boolean = true

  def getUserConfirmation(eventId: Long, userId: Long): Option[TrainingEventMemberConfirmation] = None

}
