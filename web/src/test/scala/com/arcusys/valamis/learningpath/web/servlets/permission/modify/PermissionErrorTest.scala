package com.arcusys.valamis.learningpath.web.servlets.permission.modify

import javax.servlet.http.HttpServletRequest

import com.arcusys.valamis.learningpath.ServletImpl
import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase
import com.arcusys.valamis.learningpath.web.servlets.base.PermissionChecker
import com.arcusys.valamis.learningpath.web.servlets.utils.LoggerImpl

class PermissionErrorTest extends LPServletTestBase {

  override lazy val servlet: ServletImpl = new ServletImpl(dbInfo) {
    override val log = new LoggerImpl {
      override def isErrorEnabled: Boolean = false
    }
    override val permissionChecker = new PermissionChecker() {
      override def hasPermission(permission: String)
                                (implicit r: HttpServletRequest): Boolean = {
        //some exception happens in permission checker
        ???
      }
    }
  }

  test("POST requests should be forbidden") {
    post(s"/learning-paths/") {
      status should beForbidden
      body should haveJson(s""" { "message": "no MODIFY_ACTION permission"} """)
    }
  }

  test("PUT requests should be forbidden") {
    put(s"/learning-paths/1000/draft") {
      status should beForbidden
      body should haveJson(s""" { "message": "no MODIFY_ACTION permission"} """)
    }
  }

  test("DELETE requests should be forbidden") {
    delete(s"/learning-paths/1000/") {
      status should beForbidden
      body should haveJson(s""" { "message": "no MODIFY_ACTION permission"} """)
    }
  }

}
