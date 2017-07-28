package com.arcusys.valamis.learningpath.web.servlets

import com.arcusys.valamis.learningpath.web.servlets.base.{LearningPathServletBase, PermissionChecker, Permissions}
import org.scalatra.Forbidden

/**
  * Created by mminin on 24/03/2017.
  */
trait PermissionFilter {
  self: LearningPathServletBase =>

  protected def permissionChecker: PermissionChecker

  override def requireModifyPermission(): Unit = {
    verifyPermission(Permissions.Modify)
  }

  override def hasModifyPermission: Boolean = {
    permissionChecker.hasPermission(Permissions.Modify)
  }

  private def hasPermission(permission: String): Boolean = {
    try {
      permissionChecker.hasPermission(permission)
    } catch {
      case e: Throwable =>
        log.error("can't check permission", e)
        false
    }
  }

  private def verifyPermission(permission: String): Unit = {
    if (!hasPermission(permission)) {
      halt(Forbidden(s"no $permission permission"))
    }
  }
}
