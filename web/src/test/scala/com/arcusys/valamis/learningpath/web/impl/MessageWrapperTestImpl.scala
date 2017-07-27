package com.arcusys.valamis.learningpath.web.impl

import com.arcusys.valamis.learningpath.messaging.model.MessageWrapper

/**
  * Created by pkornilov on 6/16/17.
  */
class MessageWrapperTestImpl(val msg: Map[String, AnyRef]) extends MessageWrapper[Map[String, AnyRef]] {

  override def getOriginalMessage: Map[String, AnyRef] = msg

  override def getFieldOptional(name: String): Option[AnyRef] = msg.get(name)
}
