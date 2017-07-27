package com.arcusys.valamis.learningpath.web.servlets.users

import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase
import com.arcusys.valamis.learningpath.{LiferayHelperTestImpl, ServletImpl}
import com.arcusys.valamis.members.picker.model.MemberTypes
import com.arcusys.valamis.members.picker.model.impl.ForcedUserInfo
import com.arcusys.valamis.members.picker.service.LiferayHelper

/**
  * Created by mminin on 14/03/2017.
  */
class GetLPAvailableToJoinTest  extends LPServletTestBase {

  val testUserId = 101
  val otherUserId = 102

  val userAuthHeaders = Map(("userId", testUserId.toString))

  override def servlet = new ServletImpl(dbInfo) {
    override def liferayHelper: LiferayHelper = new LiferayHelperTestImpl(
      companyId,
      users = Seq(
        ForcedUserInfo(testUserId, "user 1", "/logo1", Nil, Nil, Nil, Nil),
        ForcedUserInfo(otherUserId, "user 2", "/logo2", Nil, Nil, Nil, Nil)))
  }


  test("get with joined=false should return available to join") {
    val lp1Id = createLearningPath("lp 1")
    val lp2Id = createLearningPath("lp 2")

    addMember(lp1Id, testUserId, MemberTypes.User)
    addMember(lp2Id, otherUserId, MemberTypes.User)

    publish(lp1Id)
    publish(lp2Id)

    get("/users/current/learning-paths?joined=false", headers = userAuthHeaders) {
      status should beOk

      body should haveJson(
        s"""{
           |  "total": 1,
           |  "items": [{
           |      "id": $lp2Id,
           |      "title": "lp 2",
           |      "userMembersCount": 1,
           |      "goalsCount": 0,
           |      "hasDraft": false
           |  }]
           |}""".stripMargin)
    }
  }


  test("get available to join from empty server") {

    get("/users/current/learning-paths", ("joined", "false")) {
      status should beOk
      body should haveJson("""{ "items":[], "total":0}""")
    }
  }


  test("get available to join with filter and pagination") {

    for( i <- 1 until 100) {
      val lpId = createLearningPath("test " + i)
      if (i % 2 == 1) addMember(lpId, testUserId, MemberTypes.User)
    }

    get("/users/current/learning-paths",
      params = Seq(
        ("joined", "false"), ("title", "test 1"), ("sort", "title"), ("skip", "1"), ("take", "3")
      ),
      headers = userAuthHeaders
    ) {
      status should beOk

      body should haveJson(
        s"""{
          |  "total": 5,
          |  "items": [
          |    { "title": "test 12" },
          |    { "title": "test 14" },
          |    { "title": "test 16" }
          |  ]
          |}""".stripMargin)
    }
  }

}
