package com.arcusys.valamis.learningpath.listeners

import com.arcusys.valamis.learningpath.utils.LiferayLogSupport
import com.liferay.portal.kernel.messaging.{Message, MessageListener}
import com.liferay.portal.service.UserLocalServiceUtil

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Try

class BaseCompletedMessageListener(entityName: String, listener: CompletedListener) extends MessageListener
  with LiferayLogSupport {

  override def receive(message: Message): Unit = {
    Try {
      if (message.getString("state") == "completed") {
        for (
          entityId <- Option(message.getLong(s"${entityName}Id")).filter(_ > 0);
          userId <- Option(message.getLong("userId")).filter(_ > 0);
          user <- Option(UserLocalServiceUtil.fetchUser(userId))
        ) {
          Await.result(
            listener.onCompleted(userId, entityId)(user.getCompanyId),
            Duration.Inf
          )
        }
      }
    } recover {
      case e: Throwable => log.error(s"Failed to handle $entityName completed event: " + message, e.getMessage, e)
    }
  }
}