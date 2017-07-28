package com.arcusys.valamis.learningpath.impl.messaging

import com.arcusys.valamis.learningpath.messaging.BaseMessageListener
import com.arcusys.valamis.learningpath.utils.{LiferayLogSupport, MessageExtensions}
import com.liferay.portal.kernel.messaging.{Message, MessageBusUtil, MessageListener}

/**
  * Created by pkornilov on 6/15/17.
  */
abstract class LFBaseMessageListener extends MessageListener
    with BaseMessageListener[Message]
    with MessageExtensions
    with LiferayLogSupport {

  override def receive(message: Message): Unit = {
    super.processMessage(new MessageExt(message))
  }

  override protected def setPayload(message: Message, payload: String): Unit = {
    val responseMessage = MessageBusUtil.createResponseMessage(message)
    responseMessage.setPayload(payload)
    MessageBusUtil.sendMessage(responseMessage.getDestinationName, responseMessage)
  }
}
