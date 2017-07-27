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
class UserGroupListenerTest extends {
  val userGroup = IdAndName(203, "group")
  val testCompanyId = -1
} with LPServletTestBase {

  override lazy val servlet = new ServletImpl(dbInfo) {
    override def liferayHelper: LiferayHelper = new LiferayHelperTestImpl(
      companyId,
      userGroups = Seq(userGroup)
    )
  }

  lazy val listener = new MeberGroupsListener(servlet.dbActions, servlet.memberService)


  test("add userGroup") {
    val lpId = createLearningPath("path 1")

    addMember(lpId, userGroup.id, MemberTypes.UserGroup)

    get(s"/learning-paths/$lpId/members/groups") {
      status should beOk
      body should haveJson(""" { "total": 1 } """)
    }

    await {
      listener.onRemoved(userGroup.id, MemberTypes.UserGroup)(testCompanyId)
    }

    get(s"/learning-paths/$lpId/members/groups") {
      status should beOk
      body should haveJson(""" { "total": 0 } """)
    }
  }
}
