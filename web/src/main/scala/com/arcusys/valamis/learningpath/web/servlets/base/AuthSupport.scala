package com.arcusys.valamis.learningpath.web.servlets.base

import javax.servlet.http.HttpServletResponse

import org.scalatra.ScalatraBase
import org.scalatra.auth.{ScentryStrategy, ScentrySupport}

case class AuthUser(id: Long)

trait AuthStrategy extends ScentryStrategy[AuthUser]

trait AuthSupport extends ScentrySupport[AuthUser] {
  self: ScalatraBase =>

  protected final val StrategyName = "CustomAuth"

  before() {
    if (scentry.authenticate(StrategyName).isEmpty) {
      halt(HttpServletResponse.SC_FORBIDDEN, "You must be logged in")
    }
  }

  protected def authStrategy(app: ScalatraBase): AuthStrategy

  protected def toSession = {
    case user: AuthUser => user.id.toString
  }

  protected def fromSession = {
    case id: String => AuthUser(id.toLong)
  }

  override protected def registerAuthStrategies = {
    scentry.register(StrategyName, authStrategy)
  }

  def currentUserId = user.id
}