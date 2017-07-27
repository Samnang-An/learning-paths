package com.arcusys.valamis.learningpath.web.servlets.goals.checkers

import com.arcusys.valamis.learningpath.listeners.AssignmentListener
import com.arcusys.valamis.learningpath.models.UserStatuses
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
class AssignmentGoalCheckerTest extends LPServletTestBase {


  override def servlet: ServletImpl = new ServletImpl(dbInfo) {

    override def liferayHelper: LiferayHelper = new LiferayHelperTestImpl(
      companyId,
      users = Seq(
        ForcedUserInfo(user1Id, "user 1", "/logo/u1", Nil, Nil, Nil, Nil),
        ForcedUserInfo(user2Id, "user 2", "/logo/u1", Nil, Nil, Nil, Nil)
      ))

    override lazy val messageBusService: MessageBusService = new MessageBusServiceTestImpl(
      assignmentsData = Map(
        assignment1Id ->
          """
            |{
            |      "id": "4",
            |      "title": "Fourth assignment title",
            |      "body": "Do something",
            |      "deadline": "2017-03-23T00:00:00Z"
            |    }
          """.stripMargin,
        assignment2Id ->
          """
            |{
            |      "id": "11",
            |      "title": "Assignment eleven title",
            |      "body": "ELEVEN!",
            |      "deadline": "2017-03-30T00:00:00Z"
            |    }
          """.stripMargin
      ),
      assignmentStatusData,
      isAssignmentDeployed,
      Map(), Map(), true,
      Map(), true
    )
  }

  private val assignment1Id = 4L
  private val assignment2Id = 11L

  private val user1Id = 101L
  private val user2Id = 102L

  private var isAssignmentDeployed = true

  private var assignmentStatusData = Map(
    assignment1Id -> Map(
      user1Id -> UserStatuses.Completed,
      user2Id -> UserStatuses.WaitingForEvaluation
    ),
    assignment2Id -> Map(
      user1Id -> UserStatuses.WaitingForSubmission,
      user2Id -> UserStatuses.Completed
    )
  )

  test("check assignment goal status after publish") {
    val lpId = createLearningPath("version 1 title")

    addMember(lpId, user1Id, MemberTypes.User)
    addMember(lpId, user2Id, MemberTypes.User)

    val goal1Id = createAssignmentGoal(lpId, assignment1Id)
    val goal2Id = createAssignmentGoal(lpId, assignment2Id)
    val goalForDeletedAssignment = createAssignmentGoal(lpId, 12) //for deleted assignment

    publish(lpId)

    //User 1 passed assignment for goal 1, but not for goal 2
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
           |    "goalId": $goalForDeletedAssignment,
           |    "status": "InProgress",
           |    "startedDate": "2017-03-15T12:00:32Z",
           |    "modifiedDate": "2017-03-15T12:00:32Z"
           |  }
           |]""".stripMargin,
        ignoreValues = Seq("startedDate", "modifiedDate")
      )
    }

    //User 2 passed assignment for goal 2, but not for goal 1
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
           |    "goalId": $goalForDeletedAssignment,
           |    "status": "InProgress",
           |    "startedDate": "2017-03-15T12:00:32Z",
           |    "modifiedDate": "2017-03-15T12:00:32Z"
           |  }
           |]""".stripMargin,
        ignoreValues = Seq("startedDate", "modifiedDate")
      )
    }
  }

  test("check assignment goal status after publish when assignment is not deployed") {

    val lpId = createLearningPath("version 1 title")

    addMember(lpId, user1Id, MemberTypes.User)
    addMember(lpId, user2Id, MemberTypes.User)

    val goal1Id = createAssignmentGoal(lpId, assignment1Id)
    val goal2Id = createAssignmentGoal(lpId, assignment2Id)
    val goalForDeletedAssignment = createAssignmentGoal(lpId, 12) //for deleted assignment

    isAssignmentDeployed = false

    publish(lpId)

    //if assignments is not deployed, then all goals should be with InProgress status
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
           |    "goalId": $goalForDeletedAssignment,
           |    "status": "InProgress",
           |    "startedDate": "2017-03-15T12:00:32Z",
           |    "modifiedDate": "2017-03-15T12:00:32Z"
           |  }
           |]""".stripMargin,
        ignoreValues = Seq("startedDate", "modifiedDate")
      )
    }
  }

  test("checks that AssignmentListener uses the same logic, as the checker after publish") {

    isAssignmentDeployed = true

    val lpId = createLearningPath("version 1 title")

    addMember(lpId, user1Id, MemberTypes.User)
    addMember(lpId, user2Id, MemberTypes.User)

    val goal1Id = createAssignmentGoal(lpId, assignment1Id)
    val goal2Id = createAssignmentGoal(lpId, assignment2Id)

    publish(lpId)

    implicit val execContext = servlet.executionContext
    val assignmentListener = new AssignmentListener(servlet.dbActions, servlet.taskManager)
    //User 1 passed assignment for goal 2 after publish
    //User 2 passed assignment for goal 1 after publish

    Await.result(for {
      _ <- assignmentListener.onCompleted(user1Id, assignment2Id)(-1)
      _ <- assignmentListener.onCompleted(user2Id, assignment1Id)(-1)
    } yield (), Duration.Inf)

    //statuses aren't changed, because we just fire event without changing assignments data
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
        ignoreValues = Seq("startedDate", "modifiedDate"),
        sortBy = Some("goalId")
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
        ignoreValues = Seq("startedDate", "modifiedDate"),
        sortBy = Some("goalId")
      )
    }

  }

  test("check assignment goal status by message bus event") {

    isAssignmentDeployed = true

    val lpId = createLearningPath("version 1 title")

    addMember(lpId, user1Id, MemberTypes.User)
    addMember(lpId, user2Id, MemberTypes.User)

    val goal1Id = createAssignmentGoal(lpId, assignment1Id)
    val goal2Id = createAssignmentGoal(lpId, assignment2Id)

    publish(lpId)

    implicit val execContext = servlet.executionContext
    val assignmentListener = new AssignmentListener(servlet.dbActions, servlet.taskManager)
    //User 1 passed assignment for goal 2 after publish
    //User 2 passed assignment for goal 1 after publish

    //have to change data in the map, because AssignmentListener will
    //look there to know the status
    assignmentStatusData = Map(
      assignment1Id -> Map(
        user1Id -> UserStatuses.Completed,
        user2Id -> UserStatuses.Completed
      ),
      assignment2Id -> Map(
        user1Id -> UserStatuses.Completed,
        user2Id -> UserStatuses.Completed
      )
    )

    Await.result(for {
      _ <- assignmentListener.onCompleted(user1Id, assignment2Id)(-1)
      _ <- assignmentListener.onCompleted(user2Id, assignment1Id)(-1)
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
        ignoreValues = Seq("startedDate", "modifiedDate"),
        sortBy = Some("goalId")
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
        ignoreValues = Seq("startedDate", "modifiedDate"),
        sortBy = Some("goalId")
      )
    }

  }

}
