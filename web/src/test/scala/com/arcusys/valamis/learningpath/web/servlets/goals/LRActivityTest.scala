package com.arcusys.valamis.learningpath.web.servlets.goals

import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase

/**
  * Created by mminin on 20/02/2017.
  */
class LRActivityTest extends LPServletTestBase {
  test("create activity goal") {
    val lpId = createLearningPath("path 1")

    post(s"/learning-paths/$lpId/draft/goals/",
      s"""{
         |  "goalType": "activity",
         |  "activityName": "com.liferay.portlet.documentlibrary.model.DLFileEntry"
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beOk

      body should haveJson(
        """{
          |  "id": 1,
          |  "goalType": "activity",
          |  "indexNumber": 0,
          |  "optional": false,
          |  "modifiedDate": "2017-02-02T15:08:54Z"
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
         |  "goalType": "activity",
         |  "activityName": "com.liferay.portlet.documentlibrary.model.DLFileEntry",
         |  "optional": false,
         |  "timeLimit": "P1D",
         |  "count": 1
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beOk

      body should haveJson(
        """{
          |  "id":100,
          |  "groupId":1,
          |  "goalType":"activity",
          |  "indexNumber":0,
          |  "optional":false,
          |  "timeLimit": "P1D",
          |  "modifiedDate":"2017-02-02T15:08:54Z"
          |}
        """.stripMargin,
        ignoreValues = Seq("id", "modifiedDate")
      )
    }
  }

  test("use goal id like group id") {
    val lpId = createLearningPath("path 1")

    val lessonGoalId = createActivityGoal(lpId)

    post(s"/goal-groups/$lessonGoalId/goals/",
      s"""{
         |  "goalType": "activity",
         |  "activityName": "com.liferay.portlet.documentlibrary.model.DLFileEntry",
         |  "count": 1
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beNotFound
    }
  }

  test("update activity goal count") {
    val lpId = createLearningPath("path 1")

    val goalId = createActivityGoal(lpId, activityName = "com.testActivity", count = Some(23))

    put(s"/goals/$goalId/",
      s"""{
         |  "goalType": "activity",
         |  "optional": true,
         |  "timeLimit": "P10D",
         |  "count": 11
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beOk

      body should haveJson(
        """{
          |  "id": 100,
          |  "goalType": "activity",
          |  "indexNumber": 0,
          |  "optional": true,
          |  "timeLimit": "P10D",
          |  "modifiedDate": "2017-02-02T15:08:54Z",
          |  "activityName": "com.testActivity",
          |  "count": 11
          |}
        """.stripMargin,
        ignoreValues = Seq("id", "modifiedDate")
      )
    }
  }

  test("delete") {
    val lpId = createLearningPath("path 1")

    val goalId = createActivityGoal(lpId, activityName = "com.testActivity", count = Some(23))

    delete(s"/goals/$goalId") {
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
      s"""{
         |  "goalType": "activity",
         |  "activityName": "com.liferay.portlet.documentlibrary.model.DLFileEntry"
         |}""".stripMargin,
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
      s"""{
         |  "goalType": "activity",
         |  "activityName": "com.liferay.portlet.documentlibrary.model.DLFileEntry"
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beNotAllowed
    }
  }

  test("update published goal") {
    val lpId = createLearningPath("path 1")
    val goalId = createActivityGoal(lpId)
    publish(lpId)

    put(s"/goals/$goalId/",
      s"""{
         |  "goalType": "activity",
         |  "optional": true,
         |  "timeLimit": "P10D",
         |  "count": 11
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beNotAllowed
    }
  }

  test("delete published goal") {
    val lpId = createLearningPath("path 1")
    val goalId = createActivityGoal(lpId)
    publish(lpId)

    delete(s"/goals/$goalId") {
      status should beNotAllowed
    }
  }
}
