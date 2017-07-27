package com.arcusys.valamis.learningpath.listeners.liferay

import com.arcusys.valamis.learningpath.impl.utils.LiferayLogSupport
import com.arcusys.valamis.learningpath.init.Configuration
import com.arcusys.valamis.learningpath.listeners.StatementListener
import com.arcusys.valamis.learningpath.models.StatementInfo
import com.liferay.portal.kernel.messaging.{Message, MessageListener}
import org.joda.time.format.ISODateTimeFormat

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Try

/**
  * Created by mminin on 20/03/2017.
  */
object StatementMessageListener {
  def Destination = "valamis/lrs/statement/stored"
}

class StatementMessageListener
  extends MessageListener
    with LiferayLogSupport {

  lazy val statementListener = new StatementListener(
    Configuration.dbActions,
    Configuration.taskManager
  )(
    Configuration.executionContext
  )

  override def receive(message: Message): Unit = {
    Try {
      val userId = message.getLong("userId")

      implicit val companyId = message.getLong("companyId")

      val statementInfo = StatementInfo(
        message.getString("verbId"),
        message.getString("objectId"),
        ISODateTimeFormat.dateTime().parseDateTime(message.getString("timestamp"))
      )

      Await.result(
        statementListener.onStatementCreated(userId, statementInfo),
        Duration.Inf
      )

    } recover {
      case e: Throwable => log.error(s"Failed to handle message", e)
    }
  }
}
