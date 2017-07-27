package com.arcusys.valamis.learningpath.web.servlets.members

import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase
import com.arcusys.valamis.learningpath.{LiferayHelperTestImpl, ServletImpl}
import com.arcusys.valamis.members.picker.model.impl.ForcedUserInfo
import com.arcusys.valamis.members.picker.model.{IdAndName, MemberTypes}
import com.arcusys.valamis.members.picker.service.LiferayHelper

/**
  * Created by mminin on 02/02/2017.
  */
class GetAvailableMembersTest extends LPServletTestBase {

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
        ForcedUserInfo(2, "user 2", "/logo/u2", Nil, Nil, Nil, Nil),
        ForcedUserInfo(4, "user 4", "/logo/u4", Seq(g_3), Nil, Nil, Nil),
        ForcedUserInfo(10, "user 10", "/logo/u10", Nil, Seq(r_6), Nil, Nil),
        ForcedUserInfo(11, "user 11", "/logo/u11", Nil, Nil, Nil, Nil),
        ForcedUserInfo(12, "user 12", "/logo/u12", Nil, Nil, Seq(o_8), Nil),
        ForcedUserInfo(13, "user 13", "/logo/u13", Nil, Nil, Seq(o_8), Nil)
      ),
      roles = Seq(r_6, r_7),
      organizations = Seq(o_8, o_9),
      userGroups = Seq(g_3, g_5)
    )
  }

  test("get available user members") {
    val lpId = createLearningPath("path 1")

    addMember(lpId, 1, MemberTypes.User)
    addMember(lpId, 4, MemberTypes.User)
    addMember(lpId, 10, MemberTypes.User)
    addMember(lpId, 11, MemberTypes.User)
    addMember(lpId, 13, MemberTypes.User)

    get(s"/learning-paths/$lpId/available-members/users") {
      status should beOk
      body should haveJson(
        """{
          |  "total": 2,
          |  "items": [
          |  {
          |    "id":12,
          |    "name":"user 12",
          |    "logo":"/logo/u12",
          |    "groups":[],
          |    "roles":[],
          |    "organizations":[{"id":8,"name":"o_8"}],
          |    "membershipInfo":[]
          |  }, {
          |    "id":2,
          |    "name":"user 2",
          |    "logo":"/logo/u2",
          |    "groups":[],
          |    "roles":[],
          |    "organizations":[],
          |    "membershipInfo":[]
          |  }]
          |} """.stripMargin)
    }
  }

  test("get available role members") {
    val lpId = createLearningPath("path 1")

    addMember(lpId, 6, MemberTypes.Role)

    get(s"/learning-paths/$lpId/available-members/roles") {
      status should beOk
      body should haveJson{
        """ {"total":1,"items": [{ "id":7,"name":"r_7","userCount":0,"userPortraits":[] }] } """
      }
    }
  }

  test("get available group members") {
    val lpId = createLearningPath("path 1")

    addMember(lpId, 5, MemberTypes.UserGroup)

    get(s"/learning-paths/$lpId/available-members/groups") {
      status should beOk
      body should haveJson(
        """ {"total":1,"items": [{ "id":3,"name":"g_3","userCount":1,"userPortraits":[] }] } """
      )
    }
  }

  test("get available organization members") {
    val lpId = createLearningPath("path 1")

    addMember(lpId, 9, MemberTypes.Organization)

    get(s"/learning-paths/$lpId/available-members/organizations") {
      status should beOk
      body should haveJson(
        """ {"total":1,"items": [{ "id":8,"name":"o_8","userCount":2,"userPortraits":[] }] } """
      )
    }
  }

  test("get available users shouldn't return users from added roles groups or organizations") {
    val lpId = createLearningPath("path 1")

    addMember(lpId, 3, MemberTypes.UserGroup)
    addMember(lpId, 6, MemberTypes.Role)
    addMember(lpId, 8, MemberTypes.Organization)

    get(s"/learning-paths/$lpId/available-members/users") {
      status should beOk
      body should haveJson(
        """{
          |  "total":3,
          |  "items":[
          |    {
          |      "id":1,
          |      "name":"user 1", "logo":"/logo/u1",
          |      "groups":[], "roles":[], "organizations":[],
          |      "membershipInfo":[]
          |    },
          |    {
          |      "id":11,
          |      "name":"user 11","logo":"/logo/u11",
          |      "groups":[], "roles":[], "organizations":[],
          |      "membershipInfo":[]
          |    },
          |    {
          |      "id":2,
          |      "name":"user 2", "logo":"/logo/u2",
          |      "groups":[], "roles":[], "organizations":[],
          |      "membershipInfo":[]
          |    }
          |  ]
          |} """.stripMargin
      )
    }
  }

}
