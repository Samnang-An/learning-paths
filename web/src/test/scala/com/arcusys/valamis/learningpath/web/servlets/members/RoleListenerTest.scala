package com.arcusys.valamis.learningpath.web.servlets.members

import com.arcusys.valamis.learningpath.listeners.MeberGroupsListener
import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase
import com.arcusys.valamis.learningpath.{LiferayHelperTestImpl, ServletImpl}
import com.arcusys.valamis.members.picker.model.{IdAndName, MemberTypes}
import com.arcusys.valamis.members.picker.service.LiferayHelper
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by eboytsova on 20/04/2017.
  */
class RoleListenerTest extends {
  val role = IdAndName(203, "role")
  val testCompanyId = -1
} with LPServletTestBase {

  override lazy val servlet = new ServletImpl(dbInfo) {
    override def liferayHelper: LiferayHelper = new LiferayHelperTestImpl(
      companyId,
      roles = Seq(role)
    )
  }

  lazy val listener = new MeberGroupsListener(servlet.dbActions, servlet.memberService)

  test("add role") {
    val lpId = createLearningPath("path 1")

    addMember(lpId, role.id, MemberTypes.Role)

    get(s"/learning-paths/$lpId/members/roles") {
      status should beOk
      body should haveJson(""" { "total": 1 } """)
    }

    await {
      listener.onRemoved(role.id, MemberTypes.Role)(testCompanyId)
    }

    get(s"/learning-paths/$lpId/members/roles") {
      status should beOk
      body should haveJson(""" { "total": 0 } """)
    }
  }

}
