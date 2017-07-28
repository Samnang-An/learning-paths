package com.arcusys.valamis.learningpath.web.servlets.goals

import com.arcusys.valamis.learningpath.models.Course
import com.arcusys.valamis.learningpath.services.CourseService
import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase
import com.arcusys.valamis.learningpath.{CourseServiceTestImpl, ServletImpl}

class CourseGoalTest extends LPServletTestBase {

  override def servlet: ServletImpl = new ServletImpl(dbInfo) {
    override lazy val courseService: CourseService = new CourseServiceTestImpl(
      Seq(
        Course(11, "Course one", ""),
        Course(22, "Course two", ""),
        Course(33, "Course three", ""),
        Course(44, "abc", "")
      )
    )
  }

  test("create course goal") {
    val lpId = createLearningPath("path 1")
    val courseId = 11L

    val expected = s"""{
                      |  "id":1,
                      |  "goalType":"course",
                      |  "indexNumber":0,
                      |  "optional":false,
                      |  "modifiedDate":"2017-02-02T15:08:54Z",
                      |  "courseId": $courseId,
                      |  "title": "Course one"
                      |}
        """.stripMargin

    post(s"/learning-paths/$lpId/draft/goals/",
      s"""{
         |  "goalType": "course",
         |  "courseId": $courseId
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

  test("create course goal for non existing item") {
    val lpId = createLearningPath("path 1")
    val courseId = 55L

    val expected = s"""{
                      |  "id":1,
                      |  "goalType":"course",
                      |  "indexNumber":0,
                      |  "optional":false,
                      |  "modifiedDate":"2017-02-02T15:08:54Z",
                      |  "courseId": $courseId,
                      |  "title": "Deleted course with id $courseId"
                      |}
        """.stripMargin

    post(s"/learning-paths/$lpId/draft/goals/",
      s"""{
         |  "goalType": "course",
         |  "courseId": $courseId
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

  test("create course goal in group") {
    val lpId = createLearningPath("path 1")

    val group1Id = createGoalGroup(lpId, "group 1")
    val courseId = 22L

    post(s"/goal-groups/$group1Id/goals/",
      s"""{
         |  "goalType": "course",
         |  "courseId": $courseId,
         |  "timeLimit": "P4D"
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beOk

      body should haveJson(
        s"""{
          |  "id":100,
          |  "groupId":1,
          |  "goalType":"course",
          |  "indexNumber":0,
          |  "timeLimit": "P4D",
          |  "optional":false,
          |  "modifiedDate":"2017-02-02T15:08:54Z",
          |  "courseId": $courseId,
          |  "title": "Course two"
          |}
        """.stripMargin,
        ignoreValues = Seq("id", "modifiedDate")
      )
    }
  }

  test("use goal id like group id") {
    val lpId = createLearningPath("path 1")

    val courseGoalId = createCourseGoal(lpId, courseId = 33)

    post(s"/goal-groups/$courseGoalId/goals/",
      s"""{
         |  "goalType": "course",
         |  "courseId": 33
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beNotFound
    }
  }

  test("update course goal") {
    val lpId = createLearningPath("path 1")

    val courseId = 11

    val courseGoalId = createCourseGoal(lpId, courseId)

    val expected = s"""{
                      |  "id":100,
                      |  "goalType":"course",
                      |  "indexNumber":0,
                      |  "optional": true,
                      |  "timeLimit": "P10D",
                      |  "modifiedDate":"2017-02-02T15:08:54Z",
                      |  "courseId": $courseId,
                      |  "title": "Course one"
                      |}
        """.stripMargin

    put(s"/goals/$courseGoalId/",
      s"""{
         |  "goalType": "course",
         |  "optional": true,
         |  "timeLimit": "P10D"
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beOk

      body should haveJson(expected, ignoreValues = Seq("id", "modifiedDate"))
    }

    get(s"/learning-paths/$lpId/draft/goals/tree") {
      status should beOk
      body should haveJson(s"[$expected]", ignoreValues = Seq("id", "modifiedDate"))
    }
  }

  test("delete course goal") {
    val lpId = createLearningPath("path 1")

    val courseId = 33

    val courseGoalId = createCourseGoal(lpId, courseId)

    delete(s"/goals/$courseGoalId") {
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
      s"""{ "goalType": "course", "courseId": 11 }""",
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
      s"""{ "goalType": "course", "courseId": 11 }""",
      jsonContentHeaders
    ) {
      status should beNotAllowed
    }
  }

  test("update published course goal") {
    val lpId = createLearningPath("path 1")
    val courseId = 22
    val courseGoalId = createCourseGoal(lpId, courseId)
    publish(lpId)

    put(s"/goals/$courseGoalId/",
      s"""{ "goalType": "course", "optional": true }""".stripMargin,
      jsonContentHeaders
    ) {
      status should beNotAllowed
    }
  }

  test("delete published course goal") {
    val courseId = 22
    val lpId = createLearningPath("path 1")
    val courseGoalId = createCourseGoal(lpId, courseId)
    publish(lpId)

    delete(s"/goals/$courseGoalId") {
      status should beNotAllowed
    }
  }
}
