package com.arcusys.valamis.learningpath.web.servlets.permission.modify

import com.arcusys.valamis.learningpath.ServletImpl
import com.arcusys.valamis.learningpath.web.impl.PermissionCheckerTestImpl
import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase

class NoModifyPermissionTest extends LPServletTestBase {

  override lazy val servlet: ServletImpl = new ServletImpl(dbInfo) {
    override def permissionChecker = new PermissionCheckerTestImpl(default = false)
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
