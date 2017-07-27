package com.arcusys.valamis.learningpath.services

import com.arcusys.valamis.training.events.model.{TrainingEvent, TrainingEventMemberConfirmation}
import com.arcusys.valamis.training.events.service.TrainingService

import scala.concurrent.Future

trait TrainingEventServiceBridge {

  def isTrainingEventsDeployed(companyId: Long): Boolean

  def trainingService: TrainingService

  def getEventTitle(eventId: Long): Future[Option[String]]

  def getEvent(eventId: Long): Future[Option[TrainingEvent]]

  def isUserJoined(eventId : Long, userId : Long): Boolean

  def getUserConfirmation(eventId: Long, userId: Long): Option[TrainingEventMemberConfirmation]
}