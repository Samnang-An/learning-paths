package com.arcusys.valamis

import com.arcusys.valamis.message.broker.{MessageListener, MessageService}

import scala.concurrent.Future

/**
  * Created by pkornilov on 6/22/17.
  */
class MessageServiceTestImpl extends MessageService {
  override def init(listeners: Map[String, MessageListener]): Unit = {}

  override def sendMessage(destination: String, data: String)(implicit companyId: Long): Unit = {}

  override def sendSynchronousMessage(destination: String, data: String)
                                     (implicit companyId: Long): Future[String] = Future.successful("ok")

  override def destroy(): Unit = {}
}
