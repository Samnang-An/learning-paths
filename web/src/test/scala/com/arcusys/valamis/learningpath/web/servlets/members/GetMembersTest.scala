package com.arcusys.valamis.learningpath.web.servlets.members

import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase
import com.arcusys.valamis.learningpath.{LiferayHelperTestImpl, ServletImpl}
import com.arcusys.valamis.members.picker.model.{IdAndName, MemberTypes}
import com.arcusys.valamis.members.picker.model.impl.ForcedUserInfo
import com.arcusys.valamis.members.picker.service.LiferayHelper

/**
  * Created by mminin on 02/02/2017.
  */
class GetMembersTest extends LPServletTestBase {

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

  test("get from empty learning path") {
    val lpId = createLearningPath("path 1")

    get(s"/learning-paths/$lpId/members/users") {
      status should beOk
      body should haveJson(""" {"total":0,"items":[] } """)
    }

    get(s"/learning-paths/$lpId/members/roles") {
      status should beOk
      body should haveJson(""" {"total":0,"items":[] } """)
    }

    get(s"/learning-paths/$lpId/members/organizations") {
      status should beOk
      body should haveJson(""" {"total":0,"items":[] } """)
    }

    get(s"/learning-paths/$lpId/members/groups") {
      status should beOk
      body should haveJson(""" {"total":0,"items":[] } """)
    }
  }

  test("get user members should return added user") {
    val lpId = createLearningPath("path 1")

    addMember(lpId, 1, MemberTypes.User)

    get(s"/learning-paths/$lpId/members/users") {
      status should beOk
      body should haveJson(
        """{
          |  "total": 1,
          |  "items": [{
          |    "id":1,
          |    "name":"user 1",
          |    "logo":"/logo/u1",
          |    "groups":[],
          |    "roles":[],
          |    "organizations":[],
          |    "membershipInfo":[{"id":1,"tpe":"user"}]
          |  }]
          |} """.stripMargin)
    }
  }


  test("get group members should return added group") {
    val lpId = createLearningPath("path 1")

    addMember(lpId, 3, MemberTypes.UserGroup)

    get(s"/learning-paths/$lpId/members/groups") {
      status should beOk
      body should haveJson(
        """{
          |  "total": 1,
          |  "items":[{ "id":3, "name":"g_3", "userCount":2, "userPortraits":[] }]
          |} """.stripMargin)
    }
  }

  test("get user members should return users from added group") {
    val lpId = createLearningPath("path 1")

    addMember(lpId, 3, MemberTypes.UserGroup)

    get(s"/learning-paths/$lpId/members/users") {
      status should beOk
      body should haveJson(
        """{
          |  "total":2,
          |  "items":[{
          |    "id":2,
          |    "name":"user 2",
          |    "logo":"/logo/u2",
          |    "groups":[{
          |      "id":3,
          |      "name":"g_3"
          |    }],
          |    "roles":[],
          |    "organizations":[],
          |    "membershipInfo":[{"id":3,"tpe":"group"}]
          |  },{
          |    "id":4,
          |    "name":"user 4",
          |    "logo":"/logo/u4",
          |    "groups":[{"id":3,"name":"g_3"}],
          |    "roles":[],
          |    "organizations":[],
          |    "membershipInfo":[{"id":3,"tpe":"group"}]
          |  }]
          |} """.stripMargin)
    }
  }


  test("get role members should return added role") {
    val lpId = createLearningPath("path 1")

    addMember(lpId, 6, MemberTypes.Role)

    get(s"/learning-paths/$lpId/members/roles") {
      status should beOk
      body should haveJson(
        """{
          |  "total": 1,
          |  "items":[{ "id":6, "name":"r_6", "userCount":1, "userPortraits":[] }]
          |} """.stripMargin)
    }
  }

  test("get user members should return users from added role") {
    val lpId = createLearningPath("path 1")

    addMember(lpId, 6, MemberTypes.Role)

    get(s"/learning-paths/$lpId/members/users") {
      status should beOk
      body should haveJson(
        """{
          |  "total":1,
          |  "items":[{
          |    "id":10,
          |    "name":"user 10",
          |    "logo":"/logo/u10",
          |    "groups":[ {"id":5, "name":"g_5"} ],
          |    "roles":[ {"id":6, "name":"r_6"}, {"id":7, "name":"r_7"} ],
          |    "organizations":[],
          |    "membershipInfo":[{"id":6, "tpe":"role"}]
          |  }]
          |} """.stripMargin)
    }
  }


  test("get organization members should return added organization") {
    val lpId = createLearningPath("path 1")

    addMember(lpId, 9, MemberTypes.Organization)

    get(s"/learning-paths/$lpId/members/organizations") {
      status should beOk
      body should haveJson(
        """{
          |  "total": 1,
          |  "items":[{ "id":9, "name":"o_9", "userCount":2, "userPortraits":[] }]
          |} """.stripMargin)
    }
  }

  test("get user members should return users from added organization") {
    val lpId = createLearningPath("path 1")

    addMember(lpId, 9, MemberTypes.Organization)

    get(s"/learning-paths/$lpId/members/users") {
      status should beOk
      body should haveJson(
        """{
          |  "total":2,
          |  "items":[{
          |    "id":12,
          |    "name":"user 12",
          |    "logo":"/logo/u12",
          |    "groups":[],
          |    "roles":[],
          |    "organizations":[ {"id":8,"name":"o_8"}, {"id":9,"name":"o_9"} ],
          |    "membershipInfo":[ {"id":9,"tpe":"organization"} ]
          |  },{
          |    "id":13,
          |    "name":"user 13",
          |    "logo":"/logo/u13",
          |    "groups":[],
          |    "roles":[],
          |    "organizations":[ {"id":8,"name":"o_8"}, {"id":9,"name":"o_9"} ],
          |    "membershipInfo":[ {"id":9,"tpe":"organization"} ]
          |  }]
          |} """.stripMargin)
    }
  }


  test("get user members") {
    val lpId = createLearningPath("path 1")

    addMember(lpId, 1, MemberTypes.User)
    addMember(lpId, 2, MemberTypes.User)
    addMember(lpId, 10, MemberTypes.User)
    addMember(lpId, 3, MemberTypes.UserGroup)
    addMember(lpId, 7, MemberTypes.Role)
    addMember(lpId, 8, MemberTypes.Organization)
    addMember(lpId, 9, MemberTypes.Organization)

    get(s"/learning-paths/$lpId/members/users") {
      status should beOk
      body should haveJson(
        """{
          |  "total":7,
          |  "items": [{
          |    "id":1, "name": "user 1", "logo": "/logo/u1",
          |    "groups": [],
          |    "roles": [],
          |    "organizations": [],
          |    "membershipInfo": [{"id": 1, "tpe": "user"}]
          |  }, {
          |    "id":10, "name": "user 10", "logo": "/logo/u10",
          |    "groups": [{"id":5, "name": "g_5"}],
          |    "roles": [{"id":6, "name": "r_6"},{"id":7, "name": "r_7"}],
          |    "organizations": [],
          |    "membershipInfo": [{"id": 10, "tpe": "user"}, {"id": 7, "tpe": "role"}]
          |  }, {
          |    "id":11, "name": "user 11", "logo": "/logo/u11",
          |    "groups": [],
          |    "roles": [{"id":7, "name": "r_7"}],
          |    "organizations": [],
          |    "membershipInfo": [{"id": 7, "tpe": "role"}]
          |  }, {
          |    "id":12, "name": "user 12", "logo": "/logo/u12",
          |    "groups": [],
          |    "roles": [],
          |    "organizations": [{"id":8, "name": "o_8"},{"id":9, "name": "o_9"}],
          |    "membershipInfo": [{"id": 8, "tpe": "organization"}, {"id": 9, "tpe": "organization"}]
          |  }, {
          |    "id":13, "name": "user 13", "logo": "/logo/u13",
          |    "groups": [],
          |    "roles": [],
          |    "organizations": [{"id":8, "name": "o_8"},{"id":9, "name": "o_9"}],
          |    "membershipInfo": [{"id": 8, "tpe": "organization"}, {"id": 9, "tpe": "organization"}]
          |  }, {
          |    "id":2, "name": "user 2", "logo": "/logo/u2",
          |    "groups": [{"id":3, "name": "g_3"}],
          |    "roles": [],
          |    "organizations": [],
          |    "membershipInfo": [{"id": 2, "tpe": "user"}, {"id": 3, "tpe": "group"}]
          |  }, {
          |    "id":4, "name": "user 4", "logo": "/logo/u4",
          |    "groups": [{"id": 3, "name": "g_3"}],
          |    "roles": [],
          |    "organizations": [],
          |    "membershipInfo": [{"id": 3, "tpe": "group"}]
          |  }
          |]} """.stripMargin)
    }
  }

  test("get user members with filter sorting and paging") {
    val lpId = createLearningPath("path 1")

    addMember(lpId, 1, MemberTypes.User)
    addMember(lpId, 2, MemberTypes.User)
    addMember(lpId, 10, MemberTypes.User)
    addMember(lpId, 3, MemberTypes.UserGroup)
    addMember(lpId, 7, MemberTypes.Role)
    addMember(lpId, 8, MemberTypes.Organization)
    addMember(lpId, 9, MemberTypes.Organization)

    get(
      s"/learning-paths/$lpId/members/users" +
        "?name=1%20u" +
        "&sort=-name" +
        "&skip=1" +
        "&take=2"
    ) {
      status should beOk

      body should haveJson(
        """{
          |  "total":5,
          |  "items":[
          |    {
          |      "id":12,
          |      "name":"user 12",
          |      "logo":"/logo/u12",
          |      "groups":[],
          |      "roles":[],
          |      "organizations":[{"id":8,"name":"o_8"},{"id":9,"name":"o_9"}],
          |      "membershipInfo":[{"id":8,"tpe":"organization"},{"id":9,"tpe":"organization"}]
          |    },{
          |      "id":11,
          |      "name":"user 11",
          |      "logo":"/logo/u11",
          |      "groups":[],
          |      "roles":[{"id":7,"name":"r_7"}],
          |      "organizations":[],
          |      "membershipInfo":[{"id":7,"tpe":"role"}]
          |    }
          |  ]
          |}""".stripMargin)
    }
  }

}
