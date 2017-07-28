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
class DeleteMemberAfterPublishTest extends {
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

  test("delete user after publish") {
    val lpId = createLearningPath("path 1")
    createActivityGoal(lpId)

    addMember(lpId, user1Id, MemberTypes.User)
    addMember(lpId, user2Id, MemberTypes.User)

    publish(lpId)

    delete(s"/learning-paths/$lpId/members/users/$user2Id") {
      status should beNoContent
    }

    get(s"/learning-paths/$lpId/members/users") {
      status should beOk
      body should haveJson(
        s"""{ "total": 1, "items":[ { "id": $user1Id } ] } """
      )
    }

    get(s"/learning-paths/$lpId/members/users/$user2Id/progress/") {
      status should beNotFound
    }

    get(s"/learning-paths/$lpId/members/users/$user2Id/goals-progress/") {
      status should beNotFound
    }
  }

  test("delete user after publish, but user added different ways") {
    val lpId = createLearningPath("path 1")
    createActivityGoal(lpId)

    addMember(lpId, user3Id, MemberTypes.User)
    //add group with user 2 and user 3
    addMember(lpId, group1Id, MemberTypes.UserGroup)
    //add group with user 1 and user 3
    addMember(lpId, group2Id, MemberTypes.UserGroup)

    publish(lpId)

    delete(s"/learning-paths/$lpId/members/users/$user3Id") {
      status should beNoContent
    }

    get(s"/learning-paths/$lpId/members/users") {
      status should beOk
      body should haveJson(
        s"""{
           |  "total": 2,
           |  "items":[ { "id": $user1Id }, { "id": $user2Id } ]
           |} """.stripMargin)
    }

    get(s"/learning-paths/$lpId/members/users/$user3Id/progress/") {
      status should beNotFound
    }

    get(s"/learning-paths/$lpId/members/users/$user3Id/goals-progress/") {
      status should beNotFound
    }
  }

  test("delete user group after publish") {
    val lpId = createLearningPath("path 1")
    createActivityGoal(lpId)

    addMember(lpId, user2Id, MemberTypes.User)
    //add group with user 2 and user 3
    addMember(lpId, group1Id, MemberTypes.UserGroup)

    publish(lpId)

    //delete group with user 2 and user 3
    delete(s"/learning-paths/$lpId/members/groups/$group1Id") {
      status should beNoContent
    }

    get(s"/learning-paths/$lpId/members/users") {
      status should beOk
      body should haveJson(
        s"""{
           |  "total": 1,
           |  "items":[ { "id": $user2Id } ]
           |} """.stripMargin)
    }

    get(s"/learning-paths/$lpId/members/users/$user3Id/progress/") {
      status should beNotFound
    }

    get(s"/learning-paths/$lpId/members/users/$user3Id/goals-progress/") {
      status should beNotFound
    }
  }


  test("user leave group after publish") {
    val lpId = createLearningPath("path 1")
    createActivityGoal(lpId)

    //add group (user 2 and user 3)
    addMembers(lpId, Seq(group1Id), MemberTypes.UserGroup)

    publish(lpId)

    await {  //rise event: user 3 left group
      new MemberListener(dbInfo.db, servlet.memberService)(servlet.executionContext)
        .onUserLeaveGroup(user3Id, group1Id, MemberTypes.UserGroup)
    }

    get(s"/learning-paths/$lpId/members/users") {
      status should beOk
      body should haveJson(
        s"""{
           |  "total": 1,
           |  "items":[ { "id": $user2Id } ]
           |} """.stripMargin)
    }

    get(s"/learning-paths/$lpId/members/users/$user3Id/progress/") {
      status should beNotFound
    }

    get(s"/learning-paths/$lpId/members/users/$user3Id/goals-progress/") {
      status should beNotFound
    }
  }

  test("user leave group after publish, but user added different way") {
    val lpId = createLearningPath("path 1")
    createActivityGoal(lpId)

    //add group (user 2 and user 3)
    addMembers(lpId, Seq(group1Id), MemberTypes.UserGroup)
    addMember(lpId, user3Id, MemberTypes.User)

    publish(lpId)

    await {  //rise event: user 3 left group
      new MemberListener(dbInfo.db, servlet.memberService)(servlet.executionContext)
        .onUserLeaveGroup(user3Id, group1Id, MemberTypes.UserGroup)
    }

    get(s"/learning-paths/$lpId/members/users") {
      status should beOk
      body should haveJson(
        s"""{
           |  "total": 2,
           |  "items":[ { "id": $user2Id }, { "id": $user3Id } ]
           |} """.stripMargin)
    }

    get(s"/learning-paths/$lpId/members/users/$user3Id/progress/") {
      status should beOk
    }

    get(s"/learning-paths/$lpId/members/users/$user3Id/goals-progress/") {
      status should beOk
    }
  }
}
