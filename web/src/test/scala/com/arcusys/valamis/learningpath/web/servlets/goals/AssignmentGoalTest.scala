package com.arcusys.valamis.learningpath.web.servlets.goals

import com.arcusys.valamis.learningpath.{MessageBusServiceTestImpl, ServletImpl}
import com.arcusys.valamis.learningpath.services.MessageBusService
import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


class AssignmentGoalTest extends LPServletTestBase {

  override def servlet: ServletImpl = new ServletImpl(dbInfo) {
    override lazy val messageBusService: MessageBusService = new MessageBusServiceTestImpl(
      assignmentsData = Map(
        4L ->
          """
            |{
            |      "id": "4",
            |      "title": "Fourth assignment title",
            |      "body": "Do something",
            |      "deadline": "2017-03-23T00:00:00Z"
            |    }
          """.stripMargin,
        11L ->
          """
            |{
            |      "id": "11",
            |      "title": "Assignment eleven title",
            |      "body": "ELEVEN!",
            |      "deadline": "2017-03-30T00:00:00Z"
            |    }
          """.stripMargin,
        12L ->
          """
            |{
            |      "id": "12",
            |      "title": "Assignment twelve title",
            |      "body": "TWELVE!",
            |      "deadline": "2017-03-30T00:00:00Z"
            |    }
          """.stripMargin,
        13L ->
          """
            |{
            |      "id": "13",
            |      "title": "Assignment thirteen title",
            |      "body": "THIRTEEN!",
            |      "deadline": "2017-03-30T00:00:00Z"
            |    }
          """.stripMargin
      ),
      assignmentStatusData = Map(),
      isAssignmentDeployed = true,
      Map(), Map(), true,
      Map(), true
    )
  }

  test("create assignment goal") {
    val lpId = createLearningPath("path 1")
    val assignmentId = 11L

    val expected =
      s"""{
         |  "id":1,
         |  "goalType":"assignment",
         |  "indexNumber":0,
         |  "optional":false,
         |  "modifiedDate":"2017-02-02T15:08:54Z",
         |  "assignmentId": $assignmentId,
         |  "title": "Assignment eleven title"
         |}
        """.stripMargin

    post(s"/learning-paths/$lpId/draft/goals/",
      s"""{
         |  "goalType": "assignment",
         |  "assignmentId": $assignmentId
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beOk
      body should haveJson(expected, ignoreValues = Seq("modifiedDate"))
    }

    get(s"/learning-paths/$lpId/draft/goals/tree") {
      status should beOk
      body should haveJson(s"[$expected]", ignoreValues = Seq("modifiedDate"))
    }
  }

  /*Test deadlock on mysql. Deadlock occurred when multiple requests performed
  to create assignment, course, lesson etc. goals.*/
  test("parallel creation assignment goals") {
    val lpId = createLearningPath("path 1")
    val assignmentId1 = 11L
    val assignmentId2 = 12L
    val assignmentId3 = 13L

    val expected1 =
      s"""{
         |  "id":1,
         |  "goalType":"assignment",
         |  "indexNumber":0,
         |  "optional":false,
         |  "modifiedDate":"2017-02-02T15:08:54Z",
         |  "assignmentId": $assignmentId1,
         |  "title": "Assignment eleven title"
         |}
        """.stripMargin

    val expected2 =
      s"""{
         |  "id":2,
         |  "goalType":"assignment",
         |  "indexNumber":1,
         |  "optional":false,
         |  "modifiedDate":"2017-02-02T15:08:54Z",
         |  "assignmentId": $assignmentId2,
         |  "title": "Assignment twelve title"
         |}
        """.stripMargin

    val expected3 =
      s"""{
         |  "id":3,
         |  "goalType":"assignment",
         |  "indexNumber":2,
         |  "optional":false,
         |  "modifiedDate":"2017-02-02T15:08:54Z",
         |  "assignmentId": $assignmentId3,
         |  "title": "Assignment thirteen title"
         |}
        """.stripMargin


    val test1F = Future {
      post(s"/learning-paths/$lpId/draft/goals/",
        s"""{
           |  "goalType": "assignment",
           |  "assignmentId": $assignmentId1
           |}""".stripMargin,
        jsonContentHeaders
      ) {
        status should beOk
        body should haveJson(s"$expected1", ignoreValues = Seq("modifiedDate", "id", "indexNumber"))
      }
    }

    val test2F = Future {
      post(s"/learning-paths/$lpId/draft/goals/",
        s"""{
           |  "goalType": "assignment",
           |  "assignmentId": $assignmentId2
           |}""".stripMargin,
        jsonContentHeaders
      ) {
        status should beOk
        body should haveJson(s"$expected2", ignoreValues = Seq("modifiedDate", "id", "indexNumber"))
      }
    }

    val test3F = Future {
      post(s"/learning-paths/$lpId/draft/goals/",
        s"""{
           |  "goalType": "assignment",
           |  "assignmentId": $assignmentId3
           |}""".stripMargin,
        jsonContentHeaders
      ) {
        status should beOk
        body should haveJson(s"$expected3", ignoreValues = Seq("modifiedDate", "id", "indexNumber"))
      }
    }

    await {
      for {
        _ <- test1F
        _ <- test2F
        _ <- test3F
      } yield {
        get(s"/learning-paths/$lpId/draft/goals/tree") {
          status should beOk
          body should haveJson(s"[$expected1,$expected2,$expected3]",
            ignoreValues = Seq("modifiedDate", "id", "indexNumber"),
            sortBy = Some("assignmentId"))
        }
      }
    }
  }


  test("create assignment goal for non existing item") {
    val lpId = createLearningPath("path 1")
    val assignmentId = 999L

    val expected =
      s"""{
         |  "id":1,
         |  "goalType":"assignment",
         |  "indexNumber":0,
         |  "optional":false,
         |  "modifiedDate":"2017-02-02T15:08:54Z",
         |  "assignmentId": $assignmentId,
         |  "title": "Deleted assignment with id $assignmentId"
         |}
        """.stripMargin

    post(s"/learning-paths/$lpId/draft/goals/",
      s"""{
         |  "goalType": "assignment",
         |  "assignmentId": $assignmentId
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beOk
      body should haveJson(expected, ignoreValues = Seq("modifiedDate"))
    }

    get(s"/learning-paths/$lpId/draft/goals/tree") {
      status should beOk
      body should haveJson(s"[$expected]", ignoreValues = Seq("modifiedDate"))
    }
  }

  test("create assignment goal in group") {
    val lpId = createLearningPath("path 1")

    val group1Id = createGoalGroup(lpId, "group 1")
    val assignmentId = 11L

    post(s"/goal-groups/$group1Id/goals/",
      s"""{
         |  "goalType": "assignment",
         |  "assignmentId": $assignmentId,
         |  "timeLimit": "P4D"
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beOk

      body should haveJson(
        s"""{
           |  "id":100,
           |  "groupId":1,
           |  "goalType":"assignment",
           |  "indexNumber":0,
           |  "timeLimit": "P4D",
           |  "optional":false,
           |  "modifiedDate":"2017-02-02T15:08:54Z",
           |  "assignmentId": $assignmentId,
           |  "title": "Assignment eleven title"
           |}
        """.stripMargin,
        ignoreValues = Seq("id", "modifiedDate")
      )
    }
  }

  test("use goal id like group id") {
    val lpId = createLearningPath("path 1")

    val assignmentGoalId = createAssignmentGoal(lpId, assignmentId = 23)

    post(s"/goal-groups/$assignmentGoalId/goals/",
      s"""{
         |  "goalType": "assignment",
         |  "assignmentId": 11
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beNotFound
    }
  }

  test("update assignment goal") {
    val lpId = createLearningPath("path 1")

    val assignmentId = 11

    val assignmentGoalId = createAssignmentGoal(lpId, assignmentId)

    put(s"/goals/$assignmentGoalId/",
      s"""{
         |  "goalType": "assignment",
         |  "optional": true,
         |  "timeLimit": "P10D"
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beOk

      body should haveJson(
        s"""{
           |  "id":100,
           |  "goalType":"assignment",
           |  "indexNumber":0,
           |  "optional": true,
           |  "timeLimit": "P10D",
           |  "modifiedDate":"2017-02-02T15:08:54Z",
           |  "assignmentId": $assignmentId,
           |  "title": "Assignment eleven title"
           |}
        """.stripMargin,
        ignoreValues = Seq("id", "modifiedDate")
      )
    }
  }

  test("delete assignment goal") {
    val lpId = createLearningPath("path 1")

    val assignmentId = 23

    val assignmentGoalId = createAssignmentGoal(lpId, assignmentId)

    delete(s"/goals/$assignmentGoalId") {
      status should beNoContent
    }

    get(s"/learning-paths/$lpId/draft/goals/tree") {
      status should beOk
      body should be("[]")
    }
  }

  test("create when draft does not exist") {
    val lpId = createLearningPath("path 1")
    publish(lpId)

    post(s"/learning-paths/$lpId/draft/goals/",
      s"""{ "goalType": "assignment", "assignmentId": 11 }""",
      jsonContentHeaders
    ) {
      status should beNotFound
    }
  }

  test("create in published group") {
    val lpId = createLearningPath("path 1")
    val groupId = createGoalGroup(lpId, "group 1")
    publish(lpId)

    post(s"/goal-groups/$groupId/goals/",
      s"""{ "goalType": "assignment", "assignmentId": 11 }""",
      jsonContentHeaders
    ) {
      status should beNotAllowed
    }
  }

  test("update published assignment goal") {
    val lpId = createLearningPath("path 1")
    val assignmentId = 23
    val assignmentGoalId = createAssignmentGoal(lpId, assignmentId)
    publish(lpId)

    put(s"/goals/$assignmentGoalId/",
      s"""{ "goalType": "assignment", "optional": true }""".stripMargin,
      jsonContentHeaders
    ) {
      status should beNotAllowed
    }
  }

  test("delete published assignment goal") {
    val assignmentId = 23
    val lpId = createLearningPath("path 1")
    val assignmentGoalId = createAssignmentGoal(lpId, assignmentId)
    publish(lpId)

    delete(s"/goals/$assignmentGoalId") {
      status should beNotAllowed
    }
  }
}
