package com.arcusys.valamis.learningpath.web.servlets.users

import com.arcusys.valamis.learningpath.listeners.StatementListener
import com.arcusys.valamis.learningpath.models.StatementInfo
import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase
import com.arcusys.valamis.learningpath.{LiferayHelperTestImpl, ServletImpl}
import com.arcusys.valamis.members.picker.model.MemberTypes
import com.arcusys.valamis.members.picker.model.impl.ForcedUserInfo
import com.arcusys.valamis.members.picker.service.LiferayHelper
import org.joda.time.DateTime

/**
  * Created by mminin on 14/03/2017.
  */
class GetByStatusTest extends LPServletTestBase {

  val testUserId = 101
  val otherUserId = 102
  val defaultCompanyId = 987

  val userAuthHeaders = Map(("userId", testUserId.toString))
  val verbId = "http://verbs.org/complete"
  val objectId = "http://example.org/test_1"

  override def servlet = new ServletImpl(dbInfo) {
    override def companyId: Long = defaultCompanyId
    override def liferayHelper: LiferayHelper = new LiferayHelperTestImpl(
      defaultCompanyId,
      users = Seq(
        ForcedUserInfo(testUserId, "user 1", "/logo1", Nil, Nil, Nil, Nil),
        ForcedUserInfo(otherUserId, "user 2", "/logo2", Nil, Nil, Nil, Nil)))
  }

  lazy val statementListener =
    new StatementListener(servlet.dbActions, servlet.taskManager)(servlet.executionContext)

  test("get with with InProgress status") {
    val lp1Id = createLearningPath("lp 1")
    val lp2Id = createLearningPath("lp 2")

    createActivityGoal(lp1Id)

    addMember(lp1Id, testUserId, MemberTypes.User)
    addMember(lp2Id, otherUserId, MemberTypes.User)

    publish(lp1Id)
    publish(lp2Id)

    get("/users/current/learning-paths?status=InProgress", headers = userAuthHeaders) {
      status should beOk

      body should haveJson(
        s"""{
           |  "total": 1,
           |  "items": [{
           |      "id": $lp1Id,
           |      "title": "lp 1",
           |      "userMembersCount": 1,
           |      "hasDraft": false,
           |      "status": "InProgress"
           |  }]
           |}""".stripMargin)
    }
  }


  test("get my success learning paths") {

    val lp1Id = createLearningPath("lp 1")
    createActivityGoal(lp1Id)
    createStatementGoal(lp1Id, verbId, objectId)
    addMember(lp1Id, testUserId, MemberTypes.User)
    publish(lp1Id)

    val lp2Id = createLearningPath("lp 2")
    createActivityGoal(lp2Id)
    addMember(lp2Id, testUserId, MemberTypes.User)
    publish(lp2Id)

    val lp3Id = createLearningPath("lp 3")
    createStatementGoal(lp3Id, verbId, objectId)
    addMember(lp3Id, testUserId, MemberTypes.User)
    publish(lp3Id)

    val lp4Id = createLearningPath("lp 4")
    addMember(lp4Id, testUserId, MemberTypes.User)
    createStatementGoal(lp4Id, verbId, objectId)
    publish(lp4Id)

    await {
      statementListener.onStatementCreated(
        testUserId,
        StatementInfo(verbId, objectId, timeStamp = DateTime.now)
      )(defaultCompanyId)
    }

    get("/users/current/learning-paths?status=Success", headers = userAuthHeaders) {
      status should beOk

      body should haveJson(
        s"""{
           |  "total": 2,
           |  "items": [
           |    { "id": $lp3Id, "status": "Success" },
           |    { "id": $lp4Id, "status": "Success" }
           |  ]
           |}""".stripMargin)
    }
  }

  test("get my in progress learning paths") {

    val lp1Id = createLearningPath("lp 1")
    createActivityGoal(lp1Id)
    createStatementGoal(lp1Id, verbId, objectId)
    addMember(lp1Id, testUserId, MemberTypes.User)
    publish(lp1Id)

    val lp2Id = createLearningPath("lp 2")
    createActivityGoal(lp2Id)
    addMember(lp2Id, testUserId, MemberTypes.User)
    publish(lp2Id)

    val lp3Id = createLearningPath("lp 3")
    createStatementGoal(lp3Id, verbId, objectId)
    addMember(lp3Id, testUserId, MemberTypes.User)
    publish(lp3Id)

    val lp4Id = createLearningPath("lp 4")
    addMember(lp4Id, testUserId, MemberTypes.User)
    createStatementGoal(lp4Id, verbId, objectId)
    publish(lp4Id)

    await {
      statementListener.onStatementCreated(
        testUserId,
        StatementInfo(verbId, objectId, timeStamp = DateTime.now)
      )(defaultCompanyId)
    }

    get("/users/current/learning-paths?status=InProgress", headers = userAuthHeaders) {
      status should beOk

      body should haveJson(
        s"""{
           |  "total": 2,
           |  "items": [
           |    { "id": $lp1Id, "status": "InProgress" },
           |    { "id": $lp2Id, "status": "InProgress" }
           |  ]
           |}""".stripMargin
      )
    }
  }
}
