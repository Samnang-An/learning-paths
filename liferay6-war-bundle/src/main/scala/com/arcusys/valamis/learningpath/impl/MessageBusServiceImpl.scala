package com.arcusys.valamis.learningpath.impl

import java.util

import com.arcusys.valamis.learningpath.services.MessageBusService
import com.liferay.portal.kernel.messaging.{Message, MessageBusUtil}

import scala.util.Try

/**
  * Created by pkornilov on 3/10/17.
  */
class MessageBusServiceImpl extends MessageBusService {
  override def sendSynchronousMessage(destinationName: String, data: util.HashMap[String, AnyRef]): Try[Object] = {
    val message = new Message()
    message.setValues(data)
    Try(MessageBusUtil.sendSynchronousMessage(destinationName, message))
  }

  override def sendAsynchronousMessage(destinationName: String, data: util.HashMap[String, AnyRef]): Unit = {
    val message = new Message()
    message.setValues(data)
    MessageBusUtil.sendMessage(destinationName, message)
  }
}
