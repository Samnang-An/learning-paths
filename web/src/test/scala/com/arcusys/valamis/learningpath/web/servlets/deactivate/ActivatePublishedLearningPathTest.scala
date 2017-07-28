package com.arcusys.valamis.learningpath.web.servlets.deactivate

import com.arcusys.valamis.learningpath.listeners.{LRActivityListener, WebContentListener}
import com.arcusys.valamis.learningpath.models.LRActivityType
import com.arcusys.valamis.learningpath.{LiferayHelperTestImpl, ServletImpl}
import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase
import com.arcusys.valamis.learningpath.web.servlets.utils.{LRActivity, LRActivityTypeServiceImpl}
import com.arcusys.valamis.members.picker.model.MemberTypes
import com.arcusys.valamis.members.picker.model.impl.ForcedUserInfo
import com.arcusys.valamis.members.picker.service.LiferayHelper
import org.joda.time.DateTime
import org.json4s._
import org.json4s.jackson.JsonMethods._

class ActivatePublishedLearningPathTest extends LPServletTestBase {

  val socialActivityType = "upload"
  val companyId = 234
  val user1Id = 101

  override lazy val servlet = new ServletImpl(dbInfo) {
    override val lrActivityTypeService = new LRActivityTypeServiceImpl(
      Seq(
        LRActivityType("activityType1", "test_1"),
        LRActivityType("activityType2", "test_2")
      )
    )
    override def liferayHelper: LiferayHelper = new LiferayHelperTestImpl(
      companyId,
      users = Seq(
        ForcedUserInfo(user1Id, "user 1", "/logo/u1", Nil, Nil, Nil, Nil)
      ))
  }

  test("activate should reset start date for not completed goals") {

    val lpId = createLearningPath("test 1")

    createActivityGoal(lpId, activityName = "test_activity")
    addMember(lpId, user1Id, MemberTypes.User)

    publish(lpId)

    val startDateOriginal = get(s"/learning-paths/$lpId/members/users/$user1Id/goals-progress/") {
      status should beOk
      (parse(body) \ "startedDate").extract[DateTime]
    }

    deactivate(lpId)
    Thread.sleep(1000)
    activate(lpId)

    val startDateAfterPublish = get(s"/learning-paths/$lpId/members/users/$user1Id/goals-progress/") {
      status should beOk
      (parse(body) \ "startedDate").extract[DateTime]
    }

    assert(startDateAfterPublish isAfter startDateOriginal, ", start date was not changed")
  }

  test("activate should not touch completed goals") {

    val lpId = createLearningPath("test 1")

    createActivityGoal(lpId, activityName = socialActivityType)
    addMember(lpId, user1Id, MemberTypes.User)

    publish(lpId)

    servlet.lrActivityTypeService.addActivity(LRActivity(user1Id, socialActivityType))
    await {
      new LRActivityListener(servlet.dbActions, servlet.taskManager)(servlet.executionContext)
        .onLRActivityCreated(user1Id, socialActivityType)(companyId)
    }

    val originalGoalProgress = get(s"/learning-paths/$lpId/members/users/$user1Id/goals-progress/") {
      status should beOk
      body
    }

    deactivate(lpId)
    Thread.sleep(1000)
    activate(lpId)

    val goalProgressAfterPublish = get(s"/learning-paths/$lpId/members/users/$user1Id/goals-progress/") {
      status should beOk
      body
    }

    assert(originalGoalProgress == goalProgressAfterPublish)
  }


  test("should be impossible to completed goals when LP deactivated") {

    val lpId = createLearningPath("test 1")

    createActivityGoal(lpId, activityName = socialActivityType)
    addMember(lpId, user1Id, MemberTypes.User)

    publish(lpId)

    deactivate(lpId)

    servlet.lrActivityTypeService.addActivity(LRActivity(user1Id, socialActivityType))
    await {
      new LRActivityListener(servlet.dbActions, servlet.taskManager)(servlet.executionContext)
        .onLRActivityCreated(user1Id, socialActivityType)(companyId)
    }

    get(s"/learning-paths/$lpId/members/users/$user1Id/goals-progress/") {
      status should beOk
      body should haveJson(
        s"""[{
          |  "userId": $user1Id,
          |  "goalId": 1,
          |  "status": "InProgress"
          |}]""".stripMargin)
    }
  }

  test("should be impossible to completed goals when LP deactivated test 2") {

    val webContentId = 3123
    val lpId = createLearningPath("test 1")

    createWebContentGoal(lpId, webContentId)
    addMember(lpId, user1Id, MemberTypes.User)

    publish(lpId)

    deactivate(lpId)

    await {
      new WebContentListener(servlet.dbActions, servlet.taskManager, null)(servlet.executionContext)
        .onWebContentViewed(user1Id, webContentId)(companyId)
    }

    get(s"/learning-paths/$lpId/members/users/$user1Id/goals-progress/") {
      status should beOk
      body should haveJson(
        s"""[{
           |  "userId": $user1Id,
           |  "goalId": 1,
           |  "status": "InProgress"
           |}]""".stripMargin)
    }
  }
}
