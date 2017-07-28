package com.arcusys.valamis.learningpath.listeners.liferay

import com.arcusys.valamis.learningpath.impl.utils.LiferayLogSupport
import com.arcusys.valamis.learningpath.init.Configuration
import com.arcusys.valamis.learningpath.models.{CertificateStatuses, LPMessageActions, LPMessageFields}
import com.arcusys.valamis.learningpath.utils.JsonHelper
import com.liferay.portal.kernel.messaging.{Message, MessageBusUtil, MessageListener}
import org.joda.time.DateTime
import org.json4s.ext.{DateTimeSerializer, EnumNameSerializer}
import org.json4s.{DefaultFormats, Formats}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class LearningPathMessageListener
  extends MessageListener
    with MessageExtensions
    with LiferayLogSupport {

  implicit val jsonFormats: Formats = DefaultFormats +
    DateTimeSerializer + new EnumNameSerializer(CertificateStatuses)

  private lazy val learningPathListener = Configuration.learningPathListener

  override def receive(message: Message): Unit = {
    val responseMessage = MessageBusUtil.createResponseMessage(message)

    try {
      val action = message.getStringWithCheck(LPMessageFields.Action)

      val payload = action match {
        case LPMessageActions.IsDeployed => "true"

        case LPMessageActions.UsersToLPCount =>
          val startDate = DateTime.parse(message.getStringWithCheck(LPMessageFields.StartDate))
          val endDate = DateTime.parse(message.getStringWithCheck(LPMessageFields.EndDate))
          val companyId = message.getLongWithCheck(LPMessageFields.CompanyId)

          JsonHelper.toJson(getUsersToLPCount(startDate, endDate, companyId))

        case LPMessageActions.GetLPById =>
          val id = message.getLongWithCheck(LPMessageFields.Id)
          val companyId = message.getLongWithCheck(LPMessageFields.CompanyId)
          val learningPath = getLPById(id, companyId)
          JsonHelper.toJson(learningPath)

        case LPMessageActions.GetLPByIds =>
          val ids = message.getStringWithCheck(LPMessageFields.Ids).split(",").map(_.toLong)
          JsonHelper.toJson(getLPByIds(ids))

        case LPMessageActions.GetLPWithStatusByIds =>
          val ids = message.getStringWithCheck(LPMessageFields.Ids).split(",").map(_.toLong)
          val userId = message.getLongWithCheck(LPMessageFields.UserId)
          JsonHelper.toJson(getLPWithUserStatusByIds(userId, ids))

        case LPMessageActions.GetPassedLP =>
          val companyId = message.getLongWithCheck(LPMessageFields.CompanyId)
          val userId = message.getLongWithCheck(LPMessageFields.UserId)
          JsonHelper.toJson(getPassedLP(userId, companyId))

        case _ => throw new NoSuchMethodException(s"Action $action is not supported")
      }

      responseMessage.setPayload(payload)
    } catch {
      case ex: Throwable =>
        log.error(s"Failed to process message $message", ex)
    }

    MessageBusUtil.sendMessage(responseMessage.getDestinationName, responseMessage)
  }

  private def getUsersToLPCount(startDate: DateTime, endDate: DateTime, companyId: Long) = {
    Await.result(
      learningPathListener.getUsersToLPCount(startDate, endDate, companyId),
      Duration.Inf
    )
  }

  private def getLPById(id: Long, companyId: Long) = {
    Await.result(
      learningPathListener.getLPById(id, companyId),
      Duration.Inf
    )
  }

  private def getLPByIds(ids: Seq[Long]) =
    Await.result(learningPathListener.getLearningPathsByIds(ids), Duration.Inf)

  private def getLPWithUserStatusByIds(userId: Long, ids: Seq[Long]) =
    Await.result(learningPathListener.getLearningPathWithStatusByIds(userId, ids), Duration.Inf)

  private def getPassedLP(userId: Long, companyId: Long) =
    Await.result(learningPathListener.getPassedLearningPath(userId, companyId), Duration.Inf)
}
