package com.arcusys.valamis.learningpath.web.servlets.members

import com.arcusys.valamis.learningpath.listeners.MemberListener
import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase
import com.arcusys.valamis.learningpath.{LiferayHelperTestImpl, ServletImpl}
import com.arcusys.valamis.members.picker.model.impl.ForcedUserInfo
import com.arcusys.valamis.members.picker.model.{IdAndName, MemberTypes}
import com.arcusys.valamis.members.picker.service.LiferayHelper

/**
  * Created by mminin on 16/03/2017.
  */
class AddMemberAfterPublishTest extends {
  val user1Id = 101
  val user2Id = 102
  val user3Id = 103
  val group1Id = 201
  val group2Id = 202
} with LPServletTestBase {

  override def servlet = new ServletImpl(dbInfo) {
    private val group1 = IdAndName(group1Id, "group1")
    private val group2 = IdAndName(group2Id, "group2")

    override def liferayHelper: LiferayHelper = new LiferayHelperTestImpl(
      companyId,
      Seq(
        ForcedUserInfo(user1Id, "user 1", "/logo/u1", Seq(group2), Nil, Nil, Nil),
        ForcedUserInfo(user2Id, "user 2", "/logo/u2", Seq(group1), Nil, Nil, Nil),
        ForcedUserInfo(user3Id, "user 3", "/logo/u3", Seq(group1, group2), Nil, Nil, Nil)
      ),
      userGroups = Seq(group1, group2)
    )
  }

  test("add user after publish test") {
    val lpId = createLearningPath("path 1")
    val goalId = createActivityGoal(lpId)
    addMember(lpId, user1Id, MemberTypes.User)

    publish(lpId)

    addMember(lpId, user2Id, MemberTypes.User)

    get(s"/learning-paths/$lpId/members/users/$user2Id/progress/") {
      status should beOk

      body should haveJson(
        s"""{
           |  "learningPathId": $lpId,
           |  "status": "InProgress"
           |}
          """.stripMargin)
    }

    get(s"/learning-paths/$lpId/members/users/$user2Id/goals-progress/") {
      status should beOk

      body should haveJson(
        s"""[
           |  {
           |    "goalId": $goalId,
           |    "status": "InProgress"
           |  }
           |]""".stripMargin)
    }
  }

  test("add user group after publish test") {
    val lpId = createLearningPath("path 1")
    val goalId = createActivityGoal(lpId)
    addMember(lpId, user2Id, MemberTypes.User)

    publish(lpId)

    //add group with user 2 and user 3
    addMember(lpId, group1Id, MemberTypes.UserGroup)

    get(s"/learning-paths/$lpId/members/users/$user3Id/progress/") {
      status should beOk

      body should haveJson(
        s"""{
           |  "learningPathId": $lpId,
           |  "status": "InProgress"
           |}
          """.stripMargin)
    }

    get(s"/learning-paths/$lpId/members/users/$user3Id/goals-progress/") {
      status should beOk

      body should haveJson(
        s"""[
           |  {
           |    "goalId": $goalId,
           |    "status": "InProgress"
           |  }
           |]""".stripMargin)
    }
  }

  test("add many users after publish test") {
    val lpId = createLearningPath("path 1")
    val goalId = createActivityGoal(lpId)
    addMember(lpId, user2Id, MemberTypes.User)

    publish(lpId)
    addMembers(lpId, Seq(user1Id, user2Id), MemberTypes.User)

    get(s"/learning-paths/$lpId/members/users/$user1Id/progress/") {
      status should beOk

      body should haveJson(
        s"""{
           |  "learningPathId": $lpId,
           |  "status": "InProgress"
           |}
          """.stripMargin)
    }

    get(s"/learning-paths/$lpId/members/users/$user1Id/goals-progress/") {
      status should beOk

      body should haveJson(
        s"""[
           |  {
           |    "goalId": $goalId,
           |    "status": "InProgress"
           |  }
           |]""".stripMargin)
    }
  }

  test("add many groups after publish test") {
    val lpId = createLearningPath("path 1")
    val goalId = createActivityGoal(lpId)

    publish(lpId)
    addMembers(lpId, Seq(group1Id, group2Id), MemberTypes.UserGroup)

    get(s"/learning-paths/$lpId/members/users/$user3Id/progress/") {
      status should beOk

      body should haveJson(
        s"""{
           |  "learningPathId": $lpId,
           |  "status": "InProgress"
           |}
        """.stripMargin)
    }

    get(s"/learning-paths/$lpId/members/users/$user3Id/goals-progress/") {
      status should beOk

      body should haveJson(
        s"""[
           |  {
           |    "goalId": $goalId,
           |    "status": "InProgress"
           |  }
           |]""".stripMargin)
    }
  }

  test("add user to group after publish test") {
    val lpId = createLearningPath("path 1")
    val goalId = createActivityGoal(lpId)

    //add group (user 2 and user 3)
    addMembers(lpId, Seq(group1Id), MemberTypes.UserGroup)
    publish(lpId)

    await { //rise event: user 1 added to group 1
      new MemberListener(dbInfo.db, servlet.memberService)(servlet.executionContext)
        .onUserJoinGroup(user1Id, group1Id, MemberTypes.UserGroup)
    }

    get(s"/learning-paths/$lpId/members/users/$user1Id/progress/") {
      status should beOk

      body should haveJson(
        s"""{
           |  "learningPathId": $lpId,
           |  "status": "InProgress"
           |}
        """.stripMargin)
    }

    get(s"/learning-paths/$lpId/members/users/$user1Id/goals-progress/") {
      status should beOk

      body should haveJson(
        s"""[
           |  {
           |    "goalId": $goalId,
           |    "status": "InProgress"
           |  }
           |]""".stripMargin)
    }
  }
}
