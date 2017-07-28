package com.arcusys.valamis.learningpath.web.servlets.publish

import com.arcusys.valamis.learningpath.{LiferayHelperTestImpl, ServletImpl}
import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase
import com.arcusys.valamis.members.picker.model.{IdAndName, MemberTypes}
import com.arcusys.valamis.members.picker.model.impl.ForcedUserInfo
import com.arcusys.valamis.members.picker.service.LiferayHelper

class PublishWithUserGroupsTest extends LPServletTestBase {

  override def servlet = new ServletImpl(dbInfo) {
    private val g_3 = IdAndName(3, "g_3")
    private val g_5 = IdAndName(5, "g_5")

    override def liferayHelper: LiferayHelper = new LiferayHelperTestImpl(
      companyId,
      users = Seq(
        ForcedUserInfo(1, "user 1", "/logo/u1", Seq(g_3), Nil, Nil, Nil),
        ForcedUserInfo(2, "user 2", "/logo/u2", Seq(g_5), Nil, Nil, Nil),
        ForcedUserInfo(4, "user 4", "/logo/u4", Seq(g_3, g_5), Nil, Nil, Nil)
      ),
      userGroups = Seq(g_3, g_5)
    )
  }

  test("add intersected groups and publish") {
    val lpId = createLearningPath("version 1 title")

    addMember(lpId, 3, MemberTypes.UserGroup)
    addMember(lpId, 5, MemberTypes.UserGroup)

    publish(lpId)

    //TODO: add more constrains to check duplicate members
  }
}
