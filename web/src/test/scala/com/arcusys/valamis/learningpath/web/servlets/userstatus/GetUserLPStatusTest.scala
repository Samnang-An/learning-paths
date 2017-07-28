package com.arcusys.valamis.learningpath.web.servlets.userstatus

import com.arcusys.valamis.learningpath.listeners.LRActivityListener
import com.arcusys.valamis.learningpath.models.LRActivityType
import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase
import com.arcusys.valamis.learningpath.web.servlets.utils.{LRActivity, LRActivityTypeServiceImpl}
import com.arcusys.valamis.learningpath.{LiferayHelperTestImpl, ServletImpl}
import com.arcusys.valamis.members.picker.model.MemberTypes
import com.arcusys.valamis.members.picker.model.impl.ForcedUserInfo
import com.arcusys.valamis.members.picker.service.LiferayHelper


class GetUserLPStatusTest extends LPServletTestBase {

  val user1Id = 101
  implicit val companyId = 3453L

  override lazy val servlet = new ServletImpl(dbInfo) {
    override def liferayHelper: LiferayHelper = new LiferayHelperTestImpl(
      companyId,
      users = Seq(
        ForcedUserInfo(user1Id, "user 1", "/logo/u1", Nil, Nil, Nil, Nil)
      ))
    override val lrActivityTypeService = new LRActivityTypeServiceImpl(
      Seq(
        LRActivityType("activityType1", "test_1"),
        LRActivityType("activityType2", "test_2")
      )
    )
  }

  test("get user status") {
    val (lpId, versionId) = createLearningPathAndGetIds("version 1 title")

    addMember(lpId, user1Id, MemberTypes.User)

    createActivityGoal(lpId, activityName = "a1")

    publish(lpId)

    get(s"/learning-paths/$lpId/members/users/$user1Id/progress/") {
      status should beOk

      body should haveJson(
        s"""{
           |  "learningPathId": $lpId,
           |  "versionId": $versionId,
           |  "status": "InProgress",
           |  "progress": 0.0,
           |  "startedDate": "2017-03-13T14:37:34Z",
           |  "modifiedDate": "2017-03-13T14:37:34Z"
           |}
        """.stripMargin,
        ignoreValues = Seq("startedDate", "modifiedDate")
      )
    }
  }

  test("get user status after goals complete") {
    val socialActivityType = "blogs"
    val lpId = createLearningPath("version 1 title")

    addMember(lpId, user1Id, MemberTypes.User)
    createActivityGoal(lpId, socialActivityType, count = Some(1))

    publish(lpId)

    servlet.lrActivityTypeService.addActivity(LRActivity(user1Id, socialActivityType))
    await {
      new LRActivityListener(servlet.dbActions, servlet.taskManager)(servlet.executionContext)
        .onLRActivityCreated(user1Id, socialActivityType)(companyId)
    }

    get(s"/learning-paths/$lpId/members/users/$user1Id/progress/") {
      status should beOk

      body should haveJson(
        s"""{
           |  "learningPathId": $lpId,
           |  "status": "Success",
           |  "progress": 1.0,
           |  "startedDate": "2017-03-13T14:37:34Z",
           |  "modifiedDate": "2017-03-13T14:37:34Z"
           |}
        """.stripMargin,
        ignoreValues = Seq("startedDate", "modifiedDate")
      )
    }
  }

  test("get user status after half goals complete") {
    val socialActivityType = "blogs"
    val lpId = createLearningPath("version 1 title")

    addMember(lpId, user1Id, MemberTypes.User)

    createActivityGoal(lpId, socialActivityType, count = Some(1))
    createActivityGoal(lpId, "activityType2", count = Some(1))

    publish(lpId)

    userCreatesNewLRActivity(servlet, user1Id, socialActivityType)(companyId)

    get(s"/learning-paths/$lpId/members/users/$user1Id/progress/") {
      status should beOk

      body should haveJson(
        s"""{
           |  "learningPathId": $lpId,
           |  "status": "InProgress",
           |  "progress": 0.5,
           |  "startedDate": "2017-03-13T14:37:34Z",
           |  "modifiedDate": "2017-03-13T14:37:34Z"
           |}
        """.stripMargin,
        ignoreValues = Seq("startedDate", "modifiedDate")
      )
    }
  }

  test("learning path status should be InProgress if no goals") {
    val lpId = createLearningPath("version 1 title")

    addMember(lpId, user1Id, MemberTypes.User)

    publish(lpId)

    get(s"/learning-paths/$lpId/members/users/$user1Id/progress/") {
      status should beOk

      body should haveJson(
        s"""{
           |  "learningPathId": $lpId,
           |  "status": "InProgress",
           |  "progress": 0.0,
           |  "startedDate": "2017-03-13T14:37:34Z",
           |  "modifiedDate": "2017-03-13T14:37:34Z"
           |}
        """.stripMargin,
        ignoreValues = Seq("startedDate", "modifiedDate")
      )
    }
  }

  test("learning path status should be Success if no mandatory goals") {
    val lpId = createLearningPath("version 1 title")

    addMember(lpId, user1Id, MemberTypes.User)

    createActivityGoal(lpId, activityName = "test A", optional = true)

    publish(lpId)

    get(s"/learning-paths/$lpId/members/users/$user1Id/progress/") {
      status should beOk

      body should haveJson(
        s"""{
           |  "learningPathId": $lpId,
           |  "status": "Success",
           |  "progress": 1.0,
           |  "startedDate": "2017-03-13T14:37:34Z",
           |  "modifiedDate": "2017-03-13T14:37:34Z"
           |}
        """.stripMargin,
        ignoreValues = Seq("startedDate", "modifiedDate")
      )
    }
  }

  test("learning path status should be correct after activation") {
    val socialActivityType = "blogs"
    val lpId = createLearningPath("version 1 title")

    addMember(lpId, user1Id, MemberTypes.User)

    createActivityGoal(lpId, socialActivityType, count = Some(1))
    createActivityGoal(lpId, "activityType2", count = Some(1))

    publish(lpId)

    servlet.lrActivityTypeService.addActivity(LRActivity(user1Id, socialActivityType))
    await {
      new LRActivityListener(servlet.dbActions, servlet.taskManager)(servlet.executionContext)
        .onLRActivityCreated(user1Id, socialActivityType)(companyId)
    }

    deactivate(lpId)
    activate(lpId)

    get(s"/learning-paths/$lpId/members/users/$user1Id/progress/") {
      status should beOk

      body should haveJson(
        s"""{
           |  "learningPathId": $lpId,
           |  "status": "InProgress",
           |  "progress": 0.5,
           |  "startedDate": "2017-03-13T14:37:34Z",
           |  "modifiedDate": "2017-03-13T14:37:34Z"
           |}
        """.stripMargin,
        ignoreValues = Seq("startedDate", "modifiedDate")
      )
    }
  }


  test("learning path status should be correct after republish") {
    val socialActivityType = "blogs"
    val lpId = createLearningPath("version 1 title")

    addMember(lpId, user1Id, MemberTypes.User)

    createActivityGoal(lpId, socialActivityType, count = Some(1))
    createActivityGoal(lpId, "activityType2", count = Some(1))

    publish(lpId)

    userCreatesNewLRActivity(servlet, user1Id, socialActivityType)(companyId)

    createNewDraft(lpId)
    createActivityGoal(lpId, "activityType3", count = Some(1))
    createActivityGoal(lpId, "activityType4", count = Some(1))
    publish(lpId)

    get(s"/learning-paths/$lpId/members/users/$user1Id/progress/") {
      status should beOk

      body should haveJson(
        s"""{
           |  "learningPathId": $lpId,
           |  "status": "InProgress",
           |  "progress": 0.25,
           |  "startedDate": "2017-03-13T14:37:34Z",
           |  "modifiedDate": "2017-03-13T14:37:34Z"
           |}
        """.stripMargin,
        ignoreValues = Seq("startedDate", "modifiedDate")
      )
    }
  }

  test("learning path status should be correct user join") {
    val socialActivityType = "blogs"
    val lpId = createLearningPath("version 1 title")

    createActivityGoal(lpId, socialActivityType, count = Some(1))
    createActivityGoal(lpId, "activityType2", count = Some(1))

    publish(lpId)

    joinCurrentUser(lpId, headers = Map("userId" -> user1Id.toString))

    userCreatesNewLRActivity(servlet, user1Id, socialActivityType)(companyId)

    get(s"/learning-paths/$lpId/members/users/$user1Id/progress/") {
      status should beOk

      body should haveJson(
        s"""{
           |  "learningPathId": $lpId,
           |  "status": "InProgress",
           |  "progress": 0.5,
           |  "startedDate": "2017-03-13T14:37:34Z",
           |  "modifiedDate": "2017-03-13T14:37:34Z"
           |}
        """.stripMargin,
        ignoreValues = Seq("startedDate", "modifiedDate")
      )
    }
  }


  test("get user status from 2 level group") {
    val lpId = createLearningPath("version 1 title")

    addMember(lpId, user1Id, MemberTypes.User)

    val groupId = createGoalGroup(lpId, "group 1")

    val group1Id = createSubGoalGroup(groupId, "group 1 1")
    createActivityInGroup(group1Id, activityName = "a1")
    createActivityInGroup(group1Id, activityName = "a2")
    createActivityInGroup(group1Id, activityName = "a4")
    createActivityInGroup(group1Id, activityName = "a5")

    val group2Id = createSubGoalGroup(groupId, "group 1 2", count = Some(1))
    createActivityInGroup(group2Id, activityName = "a3")
    createActivityInGroup(group2Id, activityName = "a4")

    publish(lpId)

    userCreatesNewLRActivity(servlet, user1Id, "a2")
    userCreatesNewLRActivity(servlet, user1Id, "a3")

    get(s"/learning-paths/$lpId/members/users/$user1Id/progress/") {
      status should beOk

      body should haveJson( //complete 2 from 5 = 0.4
        s"""{
           |  "learningPathId": $lpId,
           |  "status": "InProgress",
           |  "progress": 0.4,
           |  "startedDate": "2017-03-13T14:37:34Z",
           |  "modifiedDate": "2017-03-13T14:37:34Z"
           |}
        """.stripMargin,
        ignoreValues = Seq("startedDate", "modifiedDate")
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

    get(s"/learning-paths/$lpId/members/users/$user1Id/progress/") {
      status should beOk

      body should haveJson( //complete 3 from 5 = 0.6
        s"""{
           |  "learningPathId": $lpId,
           |  "status": "InProgress",
           |  "progress": 0.6,
           |  "startedDate": "2017-03-13T14:37:34Z",
           |  "modifiedDate": "2017-03-13T14:37:34Z"
           |}
        """.stripMargin,
        ignoreValues = Seq("startedDate", "modifiedDate")
      )
    }
  }
}
