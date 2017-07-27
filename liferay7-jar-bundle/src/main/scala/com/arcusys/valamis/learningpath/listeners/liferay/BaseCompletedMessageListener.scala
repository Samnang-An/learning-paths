package com.arcusys.valamis.learningpath.listeners.liferay

import com.arcusys.valamis.learningpath.impl.utils.LiferayLogSupport
import com.arcusys.valamis.learningpath.listeners.CompletedListener
import com.liferay.portal.kernel.messaging.{Message, MessageListener}
import com.liferay.portal.kernel.service.UserLocalServiceUtil

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class BaseCompletedMessageListener(entityName: String, listener: CompletedListener) extends MessageListener
  with LiferayLogSupport {

  override def receive(message: Message): Unit = {
    try {
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
    } catch {
      case e: Throwable => log.error(s"Failed to handle $entityName completed event: " + message, e.getMessage, e)
    }
  }
}