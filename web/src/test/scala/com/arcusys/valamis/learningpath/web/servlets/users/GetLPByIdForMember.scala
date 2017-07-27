package com.arcusys.valamis.learningpath.web.servlets.users

import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase
import com.arcusys.valamis.learningpath.{LiferayHelperTestImpl, ServletImpl}
import com.arcusys.valamis.members.picker.model.MemberTypes
import com.arcusys.valamis.members.picker.model.impl.ForcedUserInfo
import com.arcusys.valamis.members.picker.service.LiferayHelper

/**
  * Created by mminin on 15/03/2017.
  */
class GetLPByIdForMember  extends LPServletTestBase {

  val testUserId = 101
  val user2Id = 102
  val user3Id = 103
  val testCompanyId = 1024L

  val userAuthHeaders = Map(("userId", testUserId.toString))

  override def servlet = new ServletImpl(dbInfo) {
    override def liferayHelper: LiferayHelper = new LiferayHelperTestImpl(
      companyId,
      users = Seq(
        ForcedUserInfo(testUserId, "user 1", "/logo/u1", Nil, Nil, Nil, Nil),
        ForcedUserInfo(user2Id, "user 2", "/logo/u2", Nil, Nil, Nil, Nil),
        ForcedUserInfo(user3Id, "user 3", "/logo/u2", Nil, Nil, Nil, Nil)
      ))
  }



  test("get form empty server") {
    get("/users/current/learning-paths/1000") {
      status should beNotFound
    }
  }

  test("should return learning path info with users and members counts") {

    val lpId = createLearningPath("test 1")

    addMember(lpId, testUserId, MemberTypes.User)
    addMember(lpId, user2Id, MemberTypes.User)
    addMember(lpId, user3Id, MemberTypes.User)

    createActivityGoal(lpId)
    createGoalGroup(lpId, "group 1")

    get(s"/users/current/learning-paths/$lpId", headers = userAuthHeaders) {
      status should beOk

      body should haveJson(
        s"""{
           |  "id": $lpId,
           |  "published":false,
           |  "title":"test 1",
           |  "openBadgesEnabled":false,
           |  "createdDate":"2017-03-14T14:17:55Z",
           |  "modifiedDate":"2017-03-14T14:17:55Z",
           |
           |  "userMembersCount": 3,
           |  "goalsCount": 2,
           |
           |  "hasDraft": true
           |}""".stripMargin,
        ignoreValues = Seq("createdDate", "modifiedDate")
      )
    }
  }

  test("should contains current user progress info") {

    val (lpId, versionId) = createLearningPathAndGetIds("test 1")

    createActivityGoal(lpId)
    createGoalGroup(lpId, "group 1")

    addMember(lpId, testUserId, MemberTypes.User)

    publish(lpId)

    get(s"/users/current/learning-paths/$lpId", headers = userAuthHeaders) {
      status should beOk

      body should haveJson(
        s"""{
           |  "id": $lpId,
           |  "published": true,
           |  "title":"test 1",
           |
           |  "status": "InProgress",
           |  "statusModifiedDate": "2017-03-23T07:49:50Z",
           |  "statusVersionId": $versionId,
           |
           |  "userMembersCount": 1,
           |  "goalsCount": 2,
           |  "hasDraft": false,
           |
           |  "createdDate": "2017-03-23T07:49:50Z",
           |  "modifiedDate": "2017-03-23T07:49:50Z"
           |}""".stripMargin,
        ignoreValues = Seq("statusModifiedDate", "createdDate", "modifiedDate")
      )
    }
  }

  test ("should ignore optional goals") {

    val (lpId, versionId) = createLearningPathAndGetIds("test 1")

    createActivityGoal(lpId, activityName = "test_activity_1", optional = true)
    createActivityGoal(lpId, activityName = "test_activity_2", optional = true)
    createActivityGoal(lpId, activityName = "test_activity_3")

    addMember(lpId, testUserId, MemberTypes.User)

    publish(lpId)

    userCreatesNewLRActivity(servlet, testUserId, "test_activity_3")(testCompanyId)

    get(s"/users/current/learning-paths/$lpId", headers = userAuthHeaders) {
      status should beOk

      body should haveJson(
        s"""{
           |  "id": $lpId,
           |  "published": true,
           |  "title":"test 1",
           |
           |  "status": "Success",
           |  "statusModifiedDate": "2017-03-23T07:49:50Z",
           |  "statusVersionId": $versionId,
           |
           |  "userMembersCount": 1,
           |  "goalsCount": 3,
           |  "hasDraft": false,
           |
           |  "createdDate": "2017-03-23T07:49:50Z",
           |  "modifiedDate": "2017-03-23T07:49:50Z"
           |}""".stripMargin,
        ignoreValues = Seq("statusModifiedDate", "createdDate", "modifiedDate")
      )
    }
  }
}
