package com.arcusys.valamis.learningpath.web.servlets.goals.checkers

import com.arcusys.valamis.learningpath.listeners.LessonListener
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
class LessonsGoalCheckerTest extends LPServletTestBase {


  override def servlet: ServletImpl = new ServletImpl(dbInfo) {

    override def liferayHelper: LiferayHelper = new LiferayHelperTestImpl(
      companyId,
      users = Seq(
        ForcedUserInfo(user1Id, "user 1", "/logo/u1", Nil, Nil, Nil, Nil),
        ForcedUserInfo(user2Id, "user 2", "/logo/u1", Nil, Nil, Nil, Nil)
      ))

    override lazy val messageBusService: MessageBusService = new MessageBusServiceTestImpl(
      Map(), Map(), true,
      Map(
        lesson1Id ->
          s"""{
                "id": $lesson1Id,
                "title": "First lesson"
              }""",
        lesson2Id ->
          s"""{
                "id": $lesson2Id,
                "title": "Second lesson"
              }"""
      ),
      lessonStatusData, isLessonDeployed,
      Map(), true
    )
  }

  private val lesson1Id = 4L
  private val lesson2Id = 11L
  private val deletedLessonId = 12L

  private val user1Id = 101L
  private val user2Id = 102L

  private var isLessonDeployed = true

  private var lessonStatusData: Map[Long, Map[Long, Boolean]] = Map(
    lesson1Id -> Map(
      user1Id -> true,
      user2Id -> false
    ),
    lesson2Id -> Map(
      user1Id -> false,
      user2Id -> true
    ),
    //real lesson service return isCompleted=false for deleted lessons
    deletedLessonId -> Map(
      user1Id -> false,
      user2Id -> false
    )
  )

  test("check lessons goal status after publish") {
    val lpId = createLearningPath("version 1 title")

    addMember(lpId, user1Id, MemberTypes.User)
    addMember(lpId, user2Id, MemberTypes.User)

    val goal1Id = createLessonGoal(lpId, lesson1Id)
    val goal2Id = createLessonGoal(lpId, lesson2Id)
    val goalForDeletedLesson = createLessonGoal(lpId, deletedLessonId) //for deleted lessons

    publish(lpId)

    //User 1 passed lessons for goal 1, but not for goal 2
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
           |    "goalId": $goalForDeletedLesson,
           |    "status": "InProgress",
           |    "startedDate": "2017-03-15T12:00:32Z",
           |    "modifiedDate": "2017-03-15T12:00:32Z"
           |  }
           |]""".stripMargin,
        ignoreValues = Seq("startedDate", "modifiedDate")
      )
    }

    //User 2 passed lesson for goal 2, but not for goal 1
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
           |    "goalId": $goalForDeletedLesson,
           |    "status": "InProgress",
           |    "startedDate": "2017-03-15T12:00:32Z",
           |    "modifiedDate": "2017-03-15T12:00:32Z"
           |  }
           |]""".stripMargin,
        ignoreValues = Seq("startedDate", "modifiedDate")
      )
    }
  }

  test("check lessons goal status after publish when valamis is not deployed") {

    val lpId = createLearningPath("version 1 title")

    addMember(lpId, user1Id, MemberTypes.User)
    addMember(lpId, user2Id, MemberTypes.User)

    val goal1Id = createLessonGoal(lpId, lesson1Id)
    val goal2Id = createLessonGoal(lpId, lesson2Id)
    val goalForDeletedLessons = createLessonGoal(lpId, deletedLessonId) //for deleted lesson

    isLessonDeployed = false

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
           |    "goalId": $goalForDeletedLessons,
           |    "status": "InProgress",
           |    "startedDate": "2017-03-15T12:00:32Z",
           |    "modifiedDate": "2017-03-15T12:00:32Z"
           |  }
           |]""".stripMargin,
        ignoreValues = Seq("startedDate", "modifiedDate")
      )
    }
  }

  test("checks that LessonListener uses the same logic, as the checker after publish") {

    isLessonDeployed = true

    val lpId = createLearningPath("version 1 title")

    addMember(lpId, user1Id, MemberTypes.User)
    addMember(lpId, user2Id, MemberTypes.User)

    val goal1Id = createLessonGoal(lpId, lesson1Id)
    val goal2Id = createLessonGoal(lpId, lesson2Id)

    publish(lpId)

    implicit val execContext = servlet.executionContext
    val lessonListener = new LessonListener(servlet.dbActions, servlet.taskManager)
    //User 1 passed lesson for goal 2 after publish
    //User 2 passed lesson for goal 1 after publish

    Await.result(for {
      _ <- lessonListener.onCompleted(user1Id, lesson2Id)(-1)
      _ <- lessonListener.onCompleted(user2Id, lesson1Id)(-1)
    } yield (), Duration.Inf)

    //statuses aren't changed, because we just fire event without changing lessons data
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

  test("check lesson goal status by message bus event") {

    isLessonDeployed = true

    val lpId = createLearningPath("version 1 title")

    addMember(lpId, user1Id, MemberTypes.User)
    addMember(lpId, user2Id, MemberTypes.User)

    val goal1Id = createLessonGoal(lpId, lesson1Id)
    val goal2Id = createLessonGoal(lpId, lesson2Id)

    publish(lpId)

    implicit val execContext = servlet.executionContext
    val lessonListener = new LessonListener(servlet.dbActions, servlet.taskManager)
    //User 1 passed lesson for goal 2 after publish
    //User 2 passed lesson for goal 1 after publish

    //have to change data in the map, because LessonListener will
    //look there to know the status
    lessonStatusData = Map(
      lesson1Id -> Map(
        user1Id -> true,
        user2Id -> true
      ),
      lesson2Id -> Map(
        user1Id -> true,
        user2Id -> true
      )
    )

    Await.result(for {
      _ <- lessonListener.onCompleted(user1Id, lesson2Id)(-1)
      _ <- lessonListener.onCompleted(user2Id, lesson1Id)(-1)
    } yield (), Duration.Inf)

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
           |    "status": "Success",
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
           |    "status": "Success",
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
