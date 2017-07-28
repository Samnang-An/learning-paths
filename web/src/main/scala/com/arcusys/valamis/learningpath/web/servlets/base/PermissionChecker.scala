package com.arcusys.valamis.learningpath.web.servlets.base

import javax.servlet.http.HttpServletRequest

/**
  * Created by mminin on 24/03/2017.
  */
trait PermissionChecker {
  def hasPermission(permission: String)
                   (implicit r: HttpServletRequest): Boolean
}

object Permissions {
  val View = "VIEW"
  val Modify = "MODIFY_ACTION"
}
