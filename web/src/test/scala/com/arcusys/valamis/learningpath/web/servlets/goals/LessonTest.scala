package com.arcusys.valamis.learningpath.web.servlets.goals

import com.arcusys.valamis.learningpath.services.MessageBusService
import com.arcusys.valamis.learningpath.{MessageBusServiceTestImpl, ServletImpl}
import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase

/**
  * Created by mminin on 02/02/2017.
  */
class LessonTest extends LPServletTestBase {

  private val lesson1Id = 4L
  private val lesson2Id = 11L

  override def servlet: ServletImpl = new ServletImpl(dbInfo) {

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
      Map(), true,
      Map(), true
    )
  }

  test("create lesson goal") {
    val lpId = createLearningPath("path 1")

    post(s"/learning-paths/$lpId/draft/goals/",
      s"""{
         |  "goalType": "lesson",
         |  "lessonId": $lesson2Id
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beOk

      body should haveJson(
        s"""{
          |  "id":1,
          |  "goalType":"lesson",
          |  "indexNumber":0,
          |  "optional":false,
          |  "modifiedDate":"2017-02-02T15:08:54Z",
          |  "lessonId": $lesson2Id,
          |  "title": "Second lesson"
          |}
        """.stripMargin,
        ignoreValues = Seq("modifiedDate")
      )
    }
  }

  test("create goal in group") {
    val lpId = createLearningPath("path 1")

    val group1Id = createGoalGroup(lpId, "group 1")

    post(s"/goal-groups/$group1Id/goals/",
      s"""{
         |  "goalType": "lesson",
         |  "lessonId": 500,
         |  "timeLimit": "P4D"
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beOk

      body should haveJson(
        """{
          |  "id":100,
          |  "groupId":1,
          |  "goalType":"lesson",
          |  "indexNumber":0,
          |  "timeLimit": "P4D",
          |  "optional":false,
          |  "modifiedDate":"2017-02-02T15:08:54Z",
          |  "lessonId": 500,
          |  "title": "Deleted lesson with id 500"
          |}
        """.stripMargin,
        ignoreValues = Seq("id", "modifiedDate")
      )
    }
  }

  test("use goal id like group id") {
    val lpId = createLearningPath("path 1")

    val lessonGoalId = createLessonGoal(lpId, lessonId = 23)

    post(s"/goal-groups/$lessonGoalId/goals/",
      s"""{
         |  "goalType": "lesson",
         |  "lessonId": 500
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beNotFound
    }
  }

  test("update lesson goal") {
    val lpId = createLearningPath("path 1")

    val lesson1Id = createLessonGoal(lpId, lessonId = 23)

    put(s"/goals/$lesson1Id/",
      s"""{
         |  "goalType": "lesson",
         |  "optional": true,
         |  "timeLimit": "P10D"
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beOk

      body should haveJson(
        """{
          |  "id":100,
          |  "goalType":"lesson",
          |  "indexNumber":0,
          |  "optional": true,
          |  "timeLimit": "P10D",
          |  "modifiedDate":"2017-02-02T15:08:54Z",
          |  "lessonId": 23,
          |  "title": "Deleted lesson with id 23"
          |}
        """.stripMargin,
        ignoreValues = Seq("id", "modifiedDate")
      )
    }
  }

  test("delete") {
    val lpId = createLearningPath("path 1")

    val lesson1Id = createLessonGoal(lpId, lessonId = 23)

    delete(s"/goals/$lesson1Id") {
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
      s"""{ "goalType": "lesson", "lessonId": 500 }""",
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
      s"""{ "goalType": "lesson", "lessonId": 500 }""",
      jsonContentHeaders
    ) {
      status should beNotAllowed
    }
  }

  test("update published lesson goal") {
    val lpId = createLearningPath("path 1")
    val lessonId = createLessonGoal(lpId, lessonId = 23)
    publish(lpId)

    put(s"/goals/$lessonId/",
      s"""{ "goalType": "lesson", "optional": true }""".stripMargin,
      jsonContentHeaders
    ) {
      status should beNotAllowed
    }
  }

  test("delete published lesson goal") {
    val lpId = createLearningPath("path 1")
    val lessonId = createLessonGoal(lpId, lessonId = 23)
    publish(lpId)

    delete(s"/goals/$lessonId") {
      status should beNotAllowed
    }
  }
}
