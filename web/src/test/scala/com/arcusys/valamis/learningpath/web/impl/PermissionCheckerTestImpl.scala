package com.arcusys.valamis.learningpath.web.impl

import javax.servlet.http.HttpServletRequest

import com.arcusys.valamis.learningpath.web.servlets.base.PermissionChecker

class PermissionCheckerTestImpl(permissions: Map[String, Boolean] = Map.empty,
                                default: Boolean = true)
  extends PermissionChecker {

  override def hasPermission(permission: String)
                            (implicit r: HttpServletRequest): Boolean = {
    permissions.getOrElse(permission, default)
  }
}
