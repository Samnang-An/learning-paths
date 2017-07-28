package com.arcusys.valamis.learningpath.web.servlets.goals.checkers

import com.arcusys.valamis.learningpath.listeners.CourseListener
import com.arcusys.valamis.learningpath.services.MessageBusService
import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase
import com.arcusys.valamis.learningpath.{LiferayHelperTestImpl, MessageBusServiceTestImpl, ServletImpl}
import com.arcusys.valamis.members.picker.model.MemberTypes
import com.arcusys.valamis.members.picker.model.impl.ForcedUserInfo
import com.arcusys.valamis.members.picker.service.LiferayHelper

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by pkornilov on 3/15/17.
  */
class CourseGoalCheckerTest extends LPServletTestBase {

  //TODO add end date test

  override lazy val servlet: ServletImpl = new ServletImpl(dbInfo) {

    override def liferayHelper: LiferayHelper = new LiferayHelperTestImpl(
      companyId,
      users = Seq(
        ForcedUserInfo(user1Id, "user 1", "/logo/u1", Nil, Nil, Nil, Nil),
        ForcedUserInfo(user2Id, "user 2", "/logo/u1", Nil, Nil, Nil, Nil)
      ))

    override lazy val messageBusService: MessageBusService = new MessageBusServiceTestImpl(
      Map(), Map(), true,
      Map(), Map(), true,
      courseStatusData, isCourseDeployed
    )
  }

  private val course1Id = 4L
  private val course2Id = 11L
  private val deletedCourseId = 12L

  private val user1Id = 101L
  private val user2Id = 102L

  @volatile
  private var isCourseDeployed = true

  private val courseStatusData: Map[Long, Map[Long, (Boolean, String)]] = Map(
    course1Id -> Map(
      user1Id -> (true, "2017-03-20T12:00:32Z"),
      user2Id -> (false, "2017-03-20T12:00:32Z")
    ),
    course2Id -> Map(
      user1Id -> (false, "2017-03-20T12:00:32Z"),
      user2Id -> (true, "2017-03-20T12:00:32Z")
    ),
    //real course service return isCompleted=false for deleted courses
    deletedCourseId -> Map(
      user1Id -> (false, "2017-03-20T12:00:32Z"),
      user2Id -> (false, "2017-03-20T12:00:32Z")
    )
  )

  test("should check course goal status after publish") {

    isCourseDeployed = true

    val lpId = createLearningPath("version 1 title")

    addMember(lpId, user1Id, MemberTypes.User)
    addMember(lpId, user2Id, MemberTypes.User)

    val goal1Id = createCourseGoal(lpId, course1Id)
    val goal2Id = createCourseGoal(lpId, course2Id)
    val goalForDeletedCourse = createCourseGoal(lpId, deletedCourseId) //for deleted courses

    publish(lpId)

    //User 1 passed course for goal 1, but not for goal 2
    get(s"/learning-paths/$lpId/members/users/$user1Id/goals-progress/") {
      status should beOk
      body should haveJson(
        s"""[
           |  {
           |    "goalId": $goal1Id,
           |    "status": "Success",
           |    "startedDate": "2017-03-15T12:00:32Z",
           |    "modifiedDate": "2017-03-15T12:00:32Z"
           |  },
           |  {
           |    "goalId": $goal2Id,
           |    "status": "InProgress",
           |    "startedDate": "2017-03-15T12:00:32Z",
           |    "modifiedDate": "2017-03-15T12:00:32Z"
           |  },
           |  {
           |    "goalId": $goalForDeletedCourse,
           |    "status": "InProgress",
           |    "startedDate": "2017-03-15T12:00:32Z",
           |    "modifiedDate": "2017-03-15T12:00:32Z"
           |  }
           |]""".stripMargin,
        ignoreValues = Seq("startedDate", "modifiedDate")
      )
    }

    //User 2 passed course for goal 2, but not for goal 1
    get(s"/learning-paths/$lpId/members/users/$user2Id/goals-progress/") {
      status should beOk

      body should haveJson(
        s"""[
           |  {
           |    "goalId": $goal1Id,
           |    "status": "InProgress",
           |    "startedDate": "2017-03-15T12:00:32Z",
           |    "modifiedDate": "2017-03-15T12:00:32Z"
           |  },
           |  {
           |    "goalId": $goal2Id,
           |    "status": "Success",
           |    "startedDate": "2017-03-15T12:00:32Z",
           |    "modifiedDate": "2017-03-15T12:00:32Z"
           |  },
           |  {
           |    "goalId": $goalForDeletedCourse,
           |    "status": "InProgress",
           |    "startedDate": "2017-03-15T12:00:32Z",
           |    "modifiedDate": "2017-03-15T12:00:32Z"
           |  }
           |]""".stripMargin,
        ignoreValues = Seq("startedDate", "modifiedDate")
      )
    }
  }

  test("should correctly check course goal status after publish when valamis is not deployed") {

    isCourseDeployed = false

    val lpId = createLearningPath("version 1 title")

    addMember(lpId, user1Id, MemberTypes.User)
    addMember(lpId, user2Id, MemberTypes.User)

    val goal1Id = createCourseGoal(lpId, course1Id)
    val goal2Id = createCourseGoal(lpId, course2Id)
    val goalForDeletedCourseId = createCourseGoal(lpId, deletedCourseId) //for deleted course

    publish(lpId)

    //if valamis is not deployed, then all goals should be with InProgress status
    get(s"/learning-paths/$lpId/members/users/$user1Id/goals-progress/") {
      status should beOk
      body should haveJson(
        s"""[
           |  {
           |    "goalId": $goal1Id,
           |    "status": "InProgress",
           |    "startedDate": "2017-03-15T12:00:32Z",
           |    "modifiedDate": "2017-03-15T12:00:32Z"
           |  },
           |  {
           |    "goalId": $goal2Id,
           |    "status": "InProgress",
           |    "startedDate": "2017-03-15T12:00:32Z",
           |    "modifiedDate": "2017-03-15T12:00:32Z"
           |  },
           |  {
           |    "goalId": $goalForDeletedCourseId,
           |    "status": "InProgress",
           |    "startedDate": "2017-03-15T12:00:32Z",
           |    "modifiedDate": "2017-03-15T12:00:32Z"
           |  }
           |]""".stripMargin,
        ignoreValues = Seq("startedDate", "modifiedDate")
      )
    }
  }

  test("CourseListener should use the same logic, as the checker after publish") {

    isCourseDeployed = true

    val lpId = createLearningPath("version 1 title")

    addMember(lpId, user1Id, MemberTypes.User)
    addMember(lpId, user2Id, MemberTypes.User)

    val goal1Id = createCourseGoal(lpId, course1Id)
    val goal2Id = createCourseGoal(lpId, course2Id)

    publish(lpId)

    implicit val execContext = servlet.executionContext
    val courseListener = new CourseListener(servlet.dbActions, servlet.taskManager)
    //User 1 passed course for goal 2 after publish
    //User 2 passed course for goal 1 after publish

    Await.result(for {
      _ <- courseListener.onCompleted(user1Id, course2Id)(-1)
      _ <- courseListener.onCompleted(user2Id, course1Id)(-1)
    } yield (), Duration.Inf)

    //statuses aren't changed, because we just fire event without changing course data
    get(s"/learning-paths/$lpId/members/users/$user1Id/goals-progress/") {
      status should beOk
      body should haveJson(
        s"""[
           |  {
           |    "goalId": $goal1Id,
           |    "status": "Success",
           |    "startedDate": "2017-03-15T12:00:32Z",
           |    "modifiedDate": "2017-03-15T12:00:32Z"
           |  },
           |  {
           |    "goalId": $goal2Id,
           |    "status": "InProgress",
           |    "startedDate": "2017-03-15T12:00:32Z",
           |    "modifiedDate": "2017-03-15T12:00:32Z"
           |  }
           |]""".stripMargin,
        ignoreValues = Seq("startedDate", "modifiedDate")
      )
    }

    get(s"/learning-paths/$lpId/members/users/$user2Id/goals-progress/") {
      status should beOk

      body should haveJson(
        s"""[
           |  {
           |    "goalId": $goal1Id,
           |    "status": "InProgress",
           |    "startedDate": "2017-03-15T12:00:32Z",
           |    "modifiedDate": "2017-03-15T12:00:32Z"
           |  },
           |  {
           |    "goalId": $goal2Id,
           |    "status": "Success",
           |    "startedDate": "2017-03-15T12:00:32Z",
           |    "modifiedDate": "2017-03-15T12:00:32Z"
           |  }
           |]""".stripMargin,
        ignoreValues = Seq("startedDate", "modifiedDate")
      )
    }
  }
}
