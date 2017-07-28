package com.arcusys.valamis.learningpath.web.servlets.users

import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase
import com.arcusys.valamis.learningpath.{LiferayHelperTestImpl, ServletImpl}
import com.arcusys.valamis.members.picker.model.MemberTypes
import com.arcusys.valamis.members.picker.model.impl.ForcedUserInfo
import com.arcusys.valamis.members.picker.service.LiferayHelper
import org.json4s.jackson.JsonMethods.parse

/**
  * Created by mminin on 15/03/2017.
  */
class GetGoalsCountByUserStatusTest  extends LPServletTestBase {

  val testUserId = 101
  val user2Id = 102
  val user3Id = 103

  implicit val companyId = 324L
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

  test("get 'success goals' count from empty server") {

    get("/users/current/success-goals-count", headers = userAuthHeaders) {
      status should beOk

      body should haveJson(""" {"value": 0 } """)
    }
  }


  test("get 'success goals' count") {

    val lp1Id = createLearningPath("test 1")
    val lp2Id = createLearningPath("test 2")
    val lp3Id = createLearningPath("test 3")

    addMember(lp1Id, testUserId, MemberTypes.User)
    addMember(lp2Id, testUserId, MemberTypes.User)

    addMember(lp1Id, user2Id, MemberTypes.User)
    addMember(lp2Id, user2Id, MemberTypes.User)

    createGoalGroup(lp1Id, "group 1")
    createActivityGoal(lp1Id, activityName = "test_activity")

    createActivityGoal(lp2Id, activityName = "test_activity")
    createActivityGoal(lp2Id, activityName = "test_activity_2")

    publish(lp1Id)
    publish(lp2Id)
    publish(lp3Id)

    userCreatesNewLRActivity(servlet, testUserId, "test_activity")
    userCreatesNewLRActivity(servlet, user2Id, "test_activity")
    userCreatesNewLRActivity(servlet, user2Id, "test_activity_2")

    get("/users/current/success-goals-count", headers = userAuthHeaders) {
      status should beOk

      body should haveJson(""" {"value": 3 } """)
    }
  }

  test("get 'success goals' should include completed and republished lp") {

    val lpId = createLearningPath("test 1")

    addMember(lpId, testUserId, MemberTypes.User)

    createActivityGoal(lpId, activityName = "test_activity")
    createActivityGoal(lpId, activityName = "test_activity_2")

    publish(lpId)

    userCreatesNewLRActivity(servlet, testUserId, "test_activity")
    userCreatesNewLRActivity(servlet, testUserId, "test_activity_2")

    // republish with new goals
    createNewDraft(lpId)

    val draftGoalIds = get(s"/learning-paths/$lpId/draft/goals/tree") {
      status should beOk
      parse(body).children.map(g => (g \ "id").extract[Long])
    }
    draftGoalIds.foreach(deleteGoal)

    createActivityGoal(lpId, activityName = "other_activity")

    publish(lpId)

    get("/users/current/success-goals-count", headers = userAuthHeaders) {
      status should beOk

      body should haveJson(""" {"value": 2 } """)
    }
  }

}
