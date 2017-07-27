package com.arcusys.valamis.learningpath.web.servlets.base

import javax.servlet.http.HttpServletResponse

import org.apache.commons.logging.Log
import org.json4s.JsonAST.{JField, JObject, JString}
import org.scalatra.{ActionResult, ResponseStatus, ScalatraServlet}
import org.scalatra.json.JacksonJsonSupport

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created by mminin on 23/01/2017.
  */
trait LearningPathServletBase
  extends ScalatraServlet
    with AuthSupport
    with JacksonJsonSupport {

  def currentUserId: Long
  implicit def companyId: Long
  protected def log: Log

  //TODO: read duration from config
  def await(f: Future[_])
           (implicit handleError: PartialFunction[Throwable, _] = PartialFunction.empty): Any = {
    Await.ready(f, Duration.Inf).value.get
      .recover(handleError)
      .get
  }

  def requireModifyPermission: Unit
  def hasModifyPermission: Boolean

  before() {
    contentType = formats("json")
  }

  error {
    case (e: Exception) =>
      log.error(e.getMessage, e)
      halt(500, "Internal server error. See details in server's log")
  }

  //convert error message to json format
  override def halt[T: Manifest](status: Integer = null,
                                 body: T = (),
                                 headers: Map[String, String] = Map.empty,
                                 reason: String = null) = {
    org.scalatra.halt(
      status,
      body match {
        case s: String => JObject(JField("message", JString(s)))
        case _ => JObject()
      },
      headers,
      reason)
  }

  // prevent: response 200 without content
  override protected def renderResponse(actionResult: Any) {
    super.renderResponse(actionResult match {
      case ActionResult(ResponseStatus(HttpServletResponse.SC_OK, _), null, headers) =>
        ActionResult(ResponseStatus(HttpServletResponse.SC_NO_CONTENT), null, headers)
      case () if status == HttpServletResponse.SC_OK =>
        status = HttpServletResponse.SC_NO_CONTENT
      case _ =>
        actionResult
    })
  }
}
