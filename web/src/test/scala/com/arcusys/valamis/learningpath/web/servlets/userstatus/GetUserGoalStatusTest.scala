package com.arcusys.valamis.learningpath.web.servlets.userstatus

import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase
import com.arcusys.valamis.learningpath.{LiferayHelperTestImpl, ServletImpl}
import com.arcusys.valamis.members.picker.model.MemberTypes
import com.arcusys.valamis.members.picker.model.impl.ForcedUserInfo
import com.arcusys.valamis.members.picker.service.LiferayHelper


class GetUserGoalStatusTest extends LPServletTestBase {

  val user1Id = 101
  implicit val companyId = 3453L

  override def servlet = new ServletImpl(dbInfo) {
    override def liferayHelper: LiferayHelper = new LiferayHelperTestImpl(
      companyId,
      users = Seq(
        ForcedUserInfo(user1Id, "user 1", "/logo/u1", Nil, Nil, Nil, Nil)
      ))
  }

  test("get user status") {
    val lpId = createLearningPath("version 1 title")

    addMember(lpId, user1Id, MemberTypes.User)

    val goal1Id = createActivityGoal(lpId, activityName = "a1")
    val goal2Id = createActivityGoal(lpId, activityName = "a2")

    publish(lpId)

    userCreatesNewLRActivity(servlet, user1Id, "a1")(companyId)

    get(s"/learning-paths/$lpId/members/users/$user1Id/goals-progress/") {
      status should beOk

      body should haveJson(
        s"""[
          |  {
          |    "goalId": $goal1Id,
          |    "status": "Success",
          |    "requiredCount": 1,
          |    "completedCount": 1
          |  },
          |  {
          |    "goalId": $goal2Id,
          |    "status": "InProgress",
          |    "requiredCount": 1,
          |    "completedCount": 0
          |  }
          |]""".stripMargin,
        ignoreValues = Seq("startedDate", "modifiedDate")
      )
    }
  }

  test("get user status if no goals in LP") {
    val lpId = createLearningPath("version 1 title")
    addMember(lpId, user1Id, MemberTypes.User)
    publish(lpId)

    get(s"/learning-paths/$lpId/members/users/$user1Id/goals-progress/") {
      status should beOk

      body should haveJson( "[ ]")
    }
  }

  test("get status if no LP") {
    val fakeId = 1000

    get(s"/learning-paths/$fakeId/members/users/$user1Id/goals-progress/") {
      status should beNotFound
    }
  }

  test("get status if user us not member of LP") {
    val lpId = createLearningPath("version 1 title")
    publish(lpId)

    val fakeUserId = 1234

    get(s"/learning-paths/$lpId/members/users/$fakeUserId/goals-progress/") {
      status should beNotFound
    }
  }

  test("get user status with group") {
    val lpId = createLearningPath("version 1 title")

    addMember(lpId, user1Id, MemberTypes.User)

    val groupId = createGoalGroup(lpId, "group 1")
    val goal1Id = createActivityInGroup(groupId, activityName = "a1")
    val goal2Id = createActivityInGroup(groupId, activityName = "a2")

    publish(lpId)

    userCreatesNewLRActivity(servlet, user1Id, "a2")(companyId)

    get(s"/learning-paths/$lpId/members/users/$user1Id/goals-progress/") {
      status should beOk

      body should haveJson(
        s"""[
           |  { "goalId": $groupId, "status": "InProgress", "requiredCount":2, "completedCount":1 },
           |  { "goalId": $goal1Id, "status": "InProgress", "requiredCount":1, "completedCount":0 },
           |  { "goalId": $goal2Id, "status": "Success", "requiredCount":1, "completedCount":1 }
           |]""".stripMargin
      )
    }
  }


  test("get user status from 2 level group") {
    val lpId = createLearningPath("version 1 title")

    addMember(lpId, user1Id, MemberTypes.User)

    val groupId = createGoalGroup(lpId, "group 1")

    val group1Id = createSubGoalGroup(groupId, "group 1 1")
    val goal1_1Id = createActivityInGroup(group1Id, activityName = "a1")
    val goal1_2Id = createActivityInGroup(group1Id, activityName = "a2")

    val group2Id = createSubGoalGroup(groupId, "group 1 2", count = Some(1))
    val goal2_1Id = createActivityInGroup(group2Id, activityName = "a3")
    val goal2_2Id = createActivityInGroup(group2Id, activityName = "a4")

    publish(lpId)

    userCreatesNewLRActivity(servlet, user1Id, "a2")
    userCreatesNewLRActivity(servlet, user1Id, "a3")

    get(s"/learning-paths/$lpId/members/users/$user1Id/goals-progress/") {
      status should beOk

      body should haveJson(
        s"""[
           |  {"goalId": $groupId, "status": "InProgress", "requiredCount":3,"completedCount":2 },
           |
           |  {"goalId": $group1Id, "status": "InProgress", "requiredCount":2,"completedCount":1 },
           |  {"goalId": $goal1_1Id, "status": "InProgress", "requiredCount":1, "completedCount":0 },
           |  {"goalId": $goal1_2Id, "status": "Success", "requiredCount":1, "completedCount":1 },
           |
           |  {"goalId": $group2Id, "status": "Success", "requiredCount":1, "completedCount":1 },
           |  {"goalId": $goal2_1Id, "status": "Success", "requiredCount":1, "completedCount":1 },
           |  {"goalId": $goal2_2Id, "status": "InProgress", "requiredCount":1,"completedCount":0 }
           |]""".stripMargin
      )
    }
  }


  test("get user status from 3 level group") {
    val lpId = createLearningPath("version 1 title")

    addMember(lpId, user1Id, MemberTypes.User)

    val groupId = createGoalGroup(lpId, "group 1")
    val goal1Id = createActivityInGroup(groupId, activityName = "a1")

    val group1Id = createSubGoalGroup(groupId, "group 1 1")
    val goal1_1Id = createActivityInGroup(group1Id, activityName = "a1")
    val goal1_2Id = createActivityInGroup(group1Id, activityName = "a2")

    val group2Id = createSubGoalGroup(groupId, "group 1 2", count = Some(1))
    val goal2_1Id = createActivityInGroup(group2Id, activityName = "a3")
    val goal2_2Id = createActivityInGroup(group2Id, activityName = "a4")

    val group3Id = createSubGoalGroup(groupId, "group 1 3", count = Some(1))
    val goal3_1Id = createActivityInGroup(group3Id, activityName = "a1")
    val goal3_2Id = createActivityInGroup(group3Id, activityName = "a2")
    val goal3_3Id = createActivityInGroup(group3Id, activityName = "a3")
    val goal3_4Id = createActivityInGroup(group3Id, activityName = "a4")

    publish(lpId)

    userCreatesNewLRActivity(servlet, user1Id, "a2")
    userCreatesNewLRActivity(servlet, user1Id, "a3")
    userCreatesNewLRActivity(servlet, user1Id, "a4")

    get(s"/learning-paths/$lpId/members/users/$user1Id/goals-progress/") {
      status should beOk

      body should haveJson(
        s"""[
           |  {"goalId": $groupId, "status": "InProgress", "requiredCount":5, "completedCount":3 },
           |
           |  {"goalId": $goal1Id, "status": "InProgress", "requiredCount":1, "completedCount":0 },
           |
           |  {"goalId": $group1Id, "status": "InProgress", "requiredCount":2, "completedCount":1 },
           |  {"goalId": $goal1_1Id, "status": "InProgress", "requiredCount":1, "completedCount":0 },
           |  {"goalId": $goal1_2Id, "status": "Success", "requiredCount":1, "completedCount":1 },
           |
           |  {"goalId": $group2Id, "status": "Success", "requiredCount":1, "completedCount":1 },
           |  {"goalId": $goal2_1Id, "status": "Success", "requiredCount":1, "completedCount":1 },
           |  {"goalId": $goal2_2Id, "status": "Success", "requiredCount":1, "completedCount":1 },
           |
           |  {"goalId": $group3Id, "status": "Success", "requiredCount":1, "completedCount":1 },
           |  {"goalId": $goal3_1Id, "status": "InProgress", "requiredCount":1, "completedCount":0 },
           |  {"goalId": $goal3_2Id, "status": "Success", "requiredCount":1, "completedCount":1 },
           |  {"goalId": $goal3_3Id, "status": "Success", "requiredCount":1, "completedCount":1 },
           |  {"goalId": $goal3_4Id, "status": "Success", "requiredCount":1, "completedCount":1 }
           |]""".stripMargin
      )
    }
  }
}
