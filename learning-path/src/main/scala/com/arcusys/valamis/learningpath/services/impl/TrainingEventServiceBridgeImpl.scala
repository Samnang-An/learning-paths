package com.arcusys.valamis.learningpath.services.impl

import java.util.Locale

import com.arcusys.valamis.learningpath.services.TrainingEventServiceBridge
import com.arcusys.valamis.learningpath.utils.DbActions
import com.arcusys.valamis.message.broker.MessageService
import com.arcusys.valamis.training.events.model.{TrainingEvent, TrainingEventMemberConfirmation}
import com.arcusys.valamis.training.events.service.TrainingService

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Try

class TrainingEventServiceBridgeImpl(val dbActions: DbActions,
                                     val messageService: MessageService,
                                     val trainingService: TrainingService)
                                    (implicit executionContext: ExecutionContext)
  extends TrainingEventServiceBridge {

  private val isDeployedAction = "isDeployed"
  private val trainingEventsDestination = "valamis/trainingEvents"

  override def isTrainingEventsDeployed(companyId: Long): Boolean = {
    val res = Try(Await.result(
      messageService.sendSynchronousMessage(trainingEventsDestination, isDeployedAction)(companyId),
      Duration.Inf
    ))
    res.toOption.contains("true")
  }

  override def getEventTitle(eventId: Long): Future[Option[String]] = Future {
    trainingService.getEvent(eventId)(Locale.getDefault) map (event => event.title)
  }

  override def getEvent(eventId: Long): Future[Option[TrainingEvent]] = Future {
    trainingService.getEvent(eventId)(Locale.getDefault)
  }

  override def isUserJoined(eventId: Long, userId: Long): Boolean = {
    trainingService.isUserJoined(eventId, userId)
  }

  override def getUserConfirmation(eventId: Long, userId: Long): Option[TrainingEventMemberConfirmation] = {
    trainingService.getUserConfirmation(eventId, userId)
  }

  private def prepareMessageData(data: Map[String, AnyRef]): java.util.HashMap[String, AnyRef] = {
    val messageValues = new java.util.HashMap[String, AnyRef]()
    data.keys.map(k => messageValues.put(k, data(k)))

    messageValues
  }
}
