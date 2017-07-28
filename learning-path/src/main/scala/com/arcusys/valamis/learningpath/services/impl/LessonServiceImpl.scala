package com.arcusys.valamis.learningpath.services.impl

import com.arcusys.valamis.learningpath.models.{LessonJsonFields, LessonMessageActionType, LessonMessageFields}
import com.arcusys.valamis.learningpath.serializer.LessonSerializer
import com.arcusys.valamis.learningpath.services.exceptions.LessonsIsNotDeployedError
import com.arcusys.valamis.learningpath.services.{LessonService, MessageBusService, MessageBusSupport}
import org.json4s.jackson.JsonMethods.parse

import scala.concurrent.Future

/**
  * Created by pkornilov on 3/15/17.
  */
class LessonServiceImpl(val messageBusService: MessageBusService) extends LessonService
  with MessageBusSupport
  with LessonSerializer {

  protected val destination = "valamis/lessons"

  private val isDeployedMessage =
    prepareMessageData(Map("action" -> LessonMessageActionType.Check.toString))

  override protected def notDeployedError: Throwable = new LessonsIsNotDeployedError

  override def getLessonNames(ids: Seq[Long]): Future[Map[Long, String]] = {
    if (ids.isEmpty) {
      Future.successful(Map())
    } else {
      Future.fromTry {
        sendMessage(Map(
          LessonMessageFields.Action -> LessonMessageActionType.LessonNames.toString,
          LessonMessageFields.LessonIds -> ids.mkString(",")
        ))(deserializeLessonNames)
      }
    }
  }

  override def isCompleted(id: Long, userId: Long)(implicit companyId: Long): Future[Boolean] = {
    Future.fromTry {
      sendMessage(Map(
        LessonMessageFields.Action -> LessonMessageActionType.LessonStatus.toString,
        LessonMessageFields.LessonId -> id.toString,
        LessonMessageFields.UserId -> userId.toString,
        LessonMessageFields.CompanyId -> companyId.toString
      )) { json =>
        (parse(json) \ LessonJsonFields.IsCompleted).extract[Boolean]
      }
    }
  }

  override def isValamisDeployed: Boolean = {
    val res = messageBusService.sendSynchronousMessage(destination, isDeployedMessage)
    res.toOption.contains("deployed")
  }
}
