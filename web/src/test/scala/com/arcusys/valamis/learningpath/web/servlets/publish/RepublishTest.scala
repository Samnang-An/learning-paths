package com.arcusys.valamis.learningpath.web.servlets.publish

import com.arcusys.valamis.learningpath.models.StatementInfo
import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase
import com.arcusys.valamis.learningpath.{LiferayHelperTestImpl, ServletImpl}
import com.arcusys.valamis.members.picker.model.MemberTypes
import com.arcusys.valamis.members.picker.model.impl.ForcedUserInfo
import com.arcusys.valamis.members.picker.service.LiferayHelper
import org.joda.time.{DateTime, Period}
import org.json4s.jackson.JsonMethods.parse

class RepublishTest extends LPServletTestBase {

  private val testUserId = 10
  private val testCompanyId = -1

  override lazy val servlet = new ServletImpl(dbInfo) {
    override lazy val liferayHelper: LiferayHelper = new LiferayHelperTestImpl(
      companyId,
      users = Seq(
        ForcedUserInfo(testUserId, "user 1", "/logo/u1", Nil, Nil, Nil, Nil)
      ))
  }

  test("should be possible to republish") {
    val lpId = createLearningPath("version 1 title")

    addMember(lpId, testUserId, MemberTypes.User)
    createLessonGoal(lpId, lessonId = 20)
    publish(lpId)
    createNewDraft(lpId)

    post(s"/learning-paths/$lpId/draft/publish") {
      status should beNoContent
    }
  }


  test("republish should reset goals status") {
    val verbId = "http://verbs.org/complete"
    val objectId = "http://example.org/test_1"
    
    val lpId = createLearningPath("test lp")

    val goal1Id = createStatementGoal(lpId, verbId, objectId, timeLimit = Some(Period.seconds(1)))
    val goal2Id = createActivityGoal(lpId, "upload_file")

    addMember(lpId, testUserId, MemberTypes.User)
    publish(lpId)

    //user fail statement goal by timeout
    Thread.sleep(2000)
    userSendsStatement(servlet, testUserId, verbId, objectId)(testCompanyId)

    get(s"/learning-paths/$lpId/members/users/$testUserId/goals-progress/") {
      status should beOk
      body should haveJson(
        s"""[
           |  { "goalId":$goal1Id, "status":"Failed" },
           |  { "goalId":$goal2Id, "status":"InProgress" }
           |]""".stripMargin)
    }

    createNewDraft(lpId)
    publish(lpId)

    get(s"/learning-paths/$lpId/members/users/$testUserId/goals-progress/") {
      status should beOk
      body should haveJson(
        s"""[
           |  { "status":"InProgress" },
           |  { "status":"InProgress" }
           |]""".stripMargin)
    }
  }


  test("republish should not affect on users with Success LP ") {
    val lpId = createLearningPath("version 1 title")

    addMember(lpId, testUserId, MemberTypes.User)
    createActivityGoal(lpId, "activity1")
    createActivityGoal(lpId, "activity2", optional = true)
    publish(lpId)

    // user complete version 1
    userCreatesNewLRActivity(servlet, testUserId, "activity1")(testCompanyId)

    val lpStatusAfterComplete = get(s"/learning-paths/$lpId/members/users/$testUserId/progress/") {
      status should beOk
      body
    }
    val goalsStatusAfterComplete = get(s"/learning-paths/$lpId/members/users/$testUserId/goals-progress/") {
      status should beOk
      body
    }

    // remove all from version 1 and create version 2
    createNewDraft(lpId)

    val draftGoalIds = get(s"/learning-paths/$lpId/draft/goals/tree") {
      status should beOk
      parse(body).children.map(g => (g \ "id").extract[Long])
    }

    draftGoalIds.foreach(deleteGoal)
    createActivityGoal(lpId, "activity1_2")
    createActivityGoal(lpId, "activity2_2", optional = true)

    publish(lpId)

    // user progress should be the same (not migrated to v2)
    val lpStatusAfterRepublish = get(s"/learning-paths/$lpId/members/users/$testUserId/progress/") {
      status should beOk
      body
    }
    val goalsStatusAfterRepublish = get(s"/learning-paths/$lpId/members/users/$testUserId/goals-progress/") {
      status should beOk
      body
    }

    assert(lpStatusAfterComplete == lpStatusAfterRepublish)
    assert(goalsStatusAfterComplete == goalsStatusAfterRepublish)
  }
}
