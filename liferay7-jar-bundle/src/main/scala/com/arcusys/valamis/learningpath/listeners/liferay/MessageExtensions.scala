package com.arcusys.valamis.learningpath.listeners.liferay

import com.arcusys.valamis.learningpath.messaging.model.MessageWrapper
import com.liferay.portal.kernel.messaging.Message

/**
  * Created by pkornilov on 3/20/17.
  */
trait MessageExtensions {

  //TODO remove implicit class when old code that uses it is refactored
  implicit class MessageExt(val msg: Message) extends MessageWrapper[Message] {

    override def getOriginalMessage: Message = msg

    override def getFieldOptional(name: String): Option[AnyRef] = Option(msg.get(name))
  }

}
