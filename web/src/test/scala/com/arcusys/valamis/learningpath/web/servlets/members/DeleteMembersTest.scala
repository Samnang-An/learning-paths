package com.arcusys.valamis.learningpath.web.servlets.members

import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase
import com.arcusys.valamis.learningpath.{LiferayHelperTestImpl, ServletImpl}
import com.arcusys.valamis.members.picker.model.impl.ForcedUserInfo
import com.arcusys.valamis.members.picker.model.{IdAndName, MemberTypes}
import com.arcusys.valamis.members.picker.service.LiferayHelper

/**
  * Created by mminin on 02/02/2017.
  */
class DeleteMembersTest extends LPServletTestBase {

  override def servlet = new ServletImpl(dbInfo) {
    private val g_3 = IdAndName(3, "g_3")
    private val g_5 = IdAndName(5, "g_5")
    private val r_6 = IdAndName(6, "r_6")
    private val r_7 = IdAndName(7, "r_7")
    private val o_8 = IdAndName(8, "o_8")
    private val o_9 = IdAndName(9, "o_9")

    override def liferayHelper: LiferayHelper = new LiferayHelperTestImpl(
      companyId,
      Seq(
        ForcedUserInfo(1, "user 1", "/logo/u1", Nil, Nil, Nil, Nil),
        ForcedUserInfo(2, "user 2", "/logo/u2", Seq(g_3), Nil, Nil, Nil),
        ForcedUserInfo(4, "user 4", "/logo/u4", Seq(g_3), Nil, Nil, Nil),
        ForcedUserInfo(10, "user 10", "/logo/u10", Seq(g_5), Seq(r_6, r_7), Nil, Nil),
        ForcedUserInfo(11, "user 11", "/logo/u11", Nil, Seq(r_7), Nil, Nil),
        ForcedUserInfo(12, "user 12", "/logo/u12", Nil, Nil, Seq(o_8, o_9), Nil),
        ForcedUserInfo(13, "user 13", "/logo/u13", Nil, Nil, Seq(o_8, o_9), Nil)
      ),
      roles = Seq(r_6, r_7),
      organizations = Seq(o_8, o_9),
      userGroups = Seq(g_3, g_5)
    )
  }

  test("delete user should remove user from members") {
    val lpId = createLearningPath("path 1")

    addMember(lpId, 1, MemberTypes.User)
    addMember(lpId, 2, MemberTypes.User)

    delete(s"/learning-paths/$lpId/members/users/1") {
      status should beNoContent
    }

    get(s"/learning-paths/$lpId/members/users") {
      status should beOk
      body should haveJson(
        """{
          |  "total": 1,
          |  "items":[{
          |    "id": 2,
          |    "name": "user 2",
          |    "logo": "/logo/u2",
          |    "groups": [],
          |    "roles": [],
          |    "organizations": [],
          |    "membershipInfo":[{"id":2,"tpe":"user"}]
          |  }]
          |} """.stripMargin)
    }
  }

  test("delete role should remove role from members") {
    val lpId = createLearningPath("path 1")

    addMember(lpId, 6, MemberTypes.Role)

    delete(s"/learning-paths/$lpId/members/roles/6") {
      status should beNoContent
    }

    get(s"/learning-paths/$lpId/members/roles") {
      status should beOk
      body should haveJson("""{ "total": 0, "items":[] } """.stripMargin)
    }
  }

  test("delete role should remove role users from members") {
    val lpId = createLearningPath("path 1")

    addMember(lpId, 6, MemberTypes.Role)

    delete(s"/learning-paths/$lpId/members/roles/6") {
      status should beNoContent
    }

    get(s"/learning-paths/$lpId/members/users") {
      status should beOk
      body should haveJson("""{ "total": 0, "items":[] } """.stripMargin)
    }
  }

  test("delete user should remove user from members if users added through group") {
    val lpId = createLearningPath("path 1")

    addMember(lpId, 8, MemberTypes.Organization)

    delete(s"/learning-paths/$lpId/members/users/12") {
      status should beNoContent
    }

    get(s"/learning-paths/$lpId/members/users") {
      status should beOk
      body should haveJson(
        """{
          |  "total": 1,
          |  "items":[{
          |    "id": 13,
          |    "name": "user 13",
          |    "logo": "/logo/u13",
          |    "groups": [],
          |    "roles": [],
          |    "organizations": [],
          |    "membershipInfo":[{"id":8,"tpe":"organization"}]
          |  }]
          |} """.stripMargin)
    }
  }

  test("delete with wrong type should return 404") {
    val lpId = createLearningPath("path 1")

    addMember(lpId, 8, MemberTypes.Organization)

    delete(s"/learning-paths/$lpId/members/wrong_type/12") {
      status should beNotFound
    }
  }

  //TODO: fix and uncomment
//  test("delete with wrong id should return 404") {
//    val lpId = createLearningPath("path 1")
//
//    delete(s"/learning-paths/$lpId/members/users/100") {
//      status should beNotFound
//    }
//  }
}
