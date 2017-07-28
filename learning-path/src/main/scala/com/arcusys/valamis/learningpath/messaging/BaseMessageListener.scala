package com.arcusys.valamis.learningpath.messaging

import com.arcusys.valamis.learningpath.messaging.model.{CommonMessageFields, MessageWrapper}
import org.apache.commons.logging.Log

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created by pkornilov on 6/15/17.
  */
trait BaseMessageListener[T] {

  protected def log: Log

  protected def processAction(action: String, message: MessageWrapper[T]): Option[String]

  protected def setPayload(message: T, payload: String): Unit

  protected def processMessage(message: MessageWrapper[T]): Unit = {
    try {
      val action = message.getStringWithCheck(CommonMessageFields.Action)
      val payload = processAction(action, message)
      payload foreach (p => setPayload(message.getOriginalMessage, p))
    } catch {
      case ex: Throwable =>
        log.error(s"Failed to process message $message", ex)
    }
  }

  protected def getId(message: MessageWrapper[T]): Long =
    message.getLongWithCheck(CommonMessageFields.Id)

  protected def getName(message: MessageWrapper[T]): String =
    message.getStringWithCheck(CommonMessageFields.Name)

  protected def getCompanyId(message: MessageWrapper[T]): Long =
    message.getLongWithCheck(CommonMessageFields.CompanyId)

  protected def await[R](f: Future[R]): R = {
    Await.result(f, Duration.Inf)
  }

}
