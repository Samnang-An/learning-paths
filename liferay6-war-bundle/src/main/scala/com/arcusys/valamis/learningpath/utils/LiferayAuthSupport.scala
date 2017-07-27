package com.arcusys.valamis.learningpath.utils

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.arcusys.valamis.learningpath.web.servlets.base.{AuthStrategy, AuthSupport, AuthUser}
import com.liferay.portal.security.auth.{CompanyThreadLocal, PrincipalThreadLocal}
import com.liferay.portal.security.permission.{PermissionCheckerFactoryUtil, PermissionThreadLocal}
import com.liferay.portal.util.PortalUtil
import org.scalatra.ScalatraBase
import org.scalatra.auth.ScentryConfig

/**
  * Created by mminin on 07/02/2017.
  */
class LiferayAuthStrategy(protected val app: ScalatraBase) extends AuthStrategy {

  def authenticate()(implicit request: HttpServletRequest,
                     response: HttpServletResponse): Option[AuthUser] = {
    Option {
      PortalUtil.getUser(request)
    }.map(user => {
      val permissionChecker = PermissionCheckerFactoryUtil.create(user)

      PermissionThreadLocal.setPermissionChecker(permissionChecker)
      PrincipalThreadLocal.setName(user.getUserId)
      CompanyThreadLocal.setCompanyId(user.getCompanyId)

      AuthUser(user.getUserId)
    })
  }
}

object LiferayAuthConfig extends ScentryConfig {
  override val login = "/c/portal/login"
  override val returnTo = "/"
  override val returnToKey = "redirect"
  override val failureUrl = "/c/portal/logout"
}

trait LiferayAuthSupport extends AuthSupport {
  self: ScalatraBase =>

  override protected val scentryConfig = LiferayAuthConfig.asInstanceOf[ScentryConfiguration]

  protected def authStrategy(app: ScalatraBase): AuthStrategy = new LiferayAuthStrategy(app)
}