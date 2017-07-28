package com.arcusys.valamis.learningpath.web.servlets

import com.arcusys.valamis.learningpath.services.MessageBusService
import com.arcusys.valamis.learningpath.{LiferayHelperTestImpl, MessageBusServiceTestImpl, ServletImpl}
import com.arcusys.valamis.members.picker.model.MemberTypes
import com.arcusys.valamis.members.picker.model.impl.ForcedUserInfo
import com.arcusys.valamis.members.picker.service.LiferayHelper
import org.json4s.jackson.JsonMethods.parse

import scala.language.postfixOps

class DeleteUncompletedGoalsTest extends LPServletTestBase {

  private val lessonId1 = 123L
  private val lessonId2 = 124L
  private val testUserId1 = 10L
  private val testUserId2 = 11L
  private implicit val companyId = -1L

  override lazy val servlet = new ServletImpl(dbInfo) {
    override lazy val liferayHelper: LiferayHelper = new LiferayHelperTestImpl(
      companyId,
      users = Seq(
        ForcedUserInfo(testUserId1, "user 1", "/logo/u1", Nil, Nil, Nil, Nil),
        ForcedUserInfo(testUserId2, "user 2", "/logo/u2", Nil, Nil, Nil, Nil)
      ))

    override lazy val messageBusService: MessageBusService = new MessageBusServiceTestImpl(
      Map(), Map(), true,
      lessonsData = Map(lessonId1 -> s"""{ "id": $lessonId1, "title": "First lesson" }"""),
      lessonStatusData = Map(lessonId1 -> Map(testUserId1 -> true, testUserId2 -> false)),
      isLessonDeployed = true,
      courseStatusData = Map(),
      isCourseDeployed = false
    )
  }


  test("LP should be completed, when we deleted uncompleted goals and left one finished goal") {
    val lpId = createLearningPath("version 1 title")

    addMember(lpId, testUserId1, MemberTypes.User)

    createActivityGoal(lpId, "act1")
    createActivityGoal(lpId, "act2")
    publish(lpId)

    userCreatesNewLRActivity(servlet, testUserId1, "act1")


    // delete not completed goal and republish
    createNewDraft(lpId)

    val uncompletedGoalId = get(s"/learning-paths/$lpId/draft/goals/tree"){
      status should beOk
      (parse(body).children.tail.head \ "id").extract[Long]
    }
    deleteGoal(uncompletedGoalId)
    publish(lpId)

    //LP should be completed
    get(s"/learning-paths/$lpId/members/users/$testUserId1/goals-progress/") {
      status should beOk
      body should haveJson( s"""[{ "status":"Success" }]""")
    }

    get(s"/learning-paths/$lpId/members/users/$testUserId1/progress/") {
      status should beOk
      body should haveJson( s"""{ "status":"Success" }""")
    }
  }



  test("activities goals test") {
    val lpId = createLearningPath("version 1 title")

    addMember(lpId, testUserId1, MemberTypes.User)
    addMember(lpId, testUserId2, MemberTypes.User)

    createActivityGoal(lpId, "act1")
    createActivityGoal(lpId, "act2")
    publish(lpId)

    userCreatesNewLRActivity(servlet, testUserId1, "act1")

    //delete second goal, that not completed by user 1
    createNewDraft(lpId)

    val activityNameToGoalId = get(s"/learning-paths/$lpId/draft/goals/tree"){
      status should beOk
      parse(body).children.map { goal =>
        val id = (goal \ "id").extract[Long]
        val activityName = (goal \ "activityName").extract[String]

        activityName -> id
      } toMap
    }
    deleteGoal(activityNameToGoalId("act2"))

    publish(lpId)


    //user 1 complete LP
    get(s"/learning-paths/$lpId/members/users/$testUserId1/goals-progress/") {
      status should beOk
      body should haveJson( """[{ "status":"Success" }]""")
    }

    get(s"/learning-paths/$lpId/members/users/$testUserId1/progress/") {
      status should beOk
      body should haveJson( """{ "status":"Success" }""")
    }

    //user 2 not complete LP
    get(s"/learning-paths/$lpId/members/users/$testUserId2/goals-progress/") {
      status should beOk
      body should haveJson( """[{ "status":"InProgress" }]""")
    }

    get(s"/learning-paths/$lpId/members/users/$testUserId2/progress/") {
      status should beOk
      body should haveJson( """{ "status":"InProgress" }""")
    }
  }



  test("LP with 2 members should be completed, when we deleted uncompleted goals and left one finished goal") {
    val lpId = createLearningPath("version 1 title")

    addMember(lpId, testUserId1, MemberTypes.User)
    addMember(lpId, testUserId2, MemberTypes.User)

    createLessonGoal(lpId, lessonId1)
    createLessonGoal(lpId, lessonId2)
    publish(lpId)

    //lesson 1 already completed by user 1

    //delete goal by lesson 2
    createNewDraft(lpId)

    val lessonIdToGoalId = get(s"/learning-paths/$lpId/draft/goals/tree"){
      status should beOk
      parse(body).children.map { goal =>
        val id = (goal \ "id").extract[Long]
        val activityName = (goal \ "lessonId").extract[Long]

        activityName -> id
      } toMap
    }
    deleteGoal(lessonIdToGoalId(lessonId2))

    publish(lpId)

    //user 1 not complete LP
    get(s"/learning-paths/$lpId/members/users/$testUserId1/goals-progress/") {
      status should beOk
      body should haveJson( """[{ "status":"Success" }]""")
    }

    get(s"/learning-paths/$lpId/members/users/$testUserId1/progress/") {
      status should beOk
      body should haveJson( """{ "status":"Success" }""")
    }


    //user 2 not complete LP
    get(s"/learning-paths/$lpId/members/users/$testUserId2/goals-progress/") {
      status should beOk
      body should haveJson( """[{ "status":"InProgress" }]""")
    }

    get(s"/learning-paths/$lpId/members/users/$testUserId2/progress/") {
      status should beOk
      body should haveJson( """{ "status":"InProgress" }""")
    }
  }
}
