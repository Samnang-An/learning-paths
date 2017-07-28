package com.arcusys.valamis.learningpath.web.servlets.users

import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase
import com.arcusys.valamis.learningpath.{LiferayHelperTestImpl, ServletImpl}
import com.arcusys.valamis.members.picker.model.MemberTypes
import com.arcusys.valamis.members.picker.model.impl.ForcedUserInfo
import com.arcusys.valamis.members.picker.service.LiferayHelper

/**
  * Created by mminin on 14/03/2017.
  */
class GetLPForJoinedMemberTest  extends LPServletTestBase {

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


  test("get with joined=true should return where current user joined") {
    val lp1Id = createLearningPath("lp 1")
    val lp2Id = createLearningPath("lp 2")

    addMember(lp1Id, testUserId, MemberTypes.User)
    addMember(lp2Id, otherUserId, MemberTypes.User)

    publish(lp1Id)
    publish(lp2Id)

    get("/users/current/learning-paths?joined=true", headers = userAuthHeaders) {
      status should beOk

      body should haveJson(
        s"""{
           |  "total": 1,
           |  "items": [{
           |      "id": $lp1Id,
           |      "title": "lp 1",
           |      "userMembersCount": 1,
           |      "goalsCount": 0,
           |      "hasDraft": false,
           |      "status": "InProgress",
           |      "statusModifiedDate": "2017-03-23T07:49:50Z"
           |  }]
           |}""".stripMargin,
        ignoreValues = Seq("statusModifiedDate")
      )
    }
  }


  test("get my learning paths from empty server") {

    get("/users/current/learning-paths", ("joined", "true")) {
      status should beOk
      body should haveJson("""{ "items":[], "total":0}""")
    }
  }


  test("get my learning paths with filter and pagination") {

    for( i <- 1 until 100) {
      val lpId = createLearningPath("test " + i)
      if (i % 2 == 1) addMember(lpId, testUserId, MemberTypes.User)
    }

    get("/users/current/learning-paths",
      params = Seq(
        ("joined", "true"), ("title", "test 2"), ("sort", "title"), ("skip", "1"), ("take", "3")
      ),
      headers = userAuthHeaders
    ) {
      status should beOk

      body should haveJson(
        s"""{
          |  "total": 5,
          |  "items": [
          |    { "title": "test 23" },
          |    { "title": "test 25" },
          |    { "title": "test 27" }
          |  ]
          |}""".stripMargin)
    }
  }

}
