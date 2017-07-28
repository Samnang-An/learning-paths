package com.arcusys.valamis.learningpath.web.impl

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.arcusys.valamis.learningpath.web.servlets.base.{AuthStrategy, AuthUser}
import org.scalatra.ScalatraBase
import org.scalatra.auth.ScentryConfig

import scala.util.Try


class AuthStrategyTestImpl(protected val app: ScalatraBase) extends AuthStrategy {

  def authenticate()(implicit request: HttpServletRequest,
                     response: HttpServletResponse): Option[AuthUser] = {

    val userId = Try {
      request.getHeader("userId").toLong
    } getOrElse {
      -1L
    }

    Some(AuthUser(userId))
  }
}

object LiferayAuthConfig extends ScentryConfig {
  override val login = "/c/portal/login"
  override val returnTo = "/"
  override val returnToKey = "redirect"
  override val failureUrl = "/c/portal/logout"
}
