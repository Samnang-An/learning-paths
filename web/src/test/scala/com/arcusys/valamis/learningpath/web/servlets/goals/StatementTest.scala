package com.arcusys.valamis.learningpath.web.servlets.goals

import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase

/**
  * Created by mminin on 02/02/2017.
  */
class StatementTest extends LPServletTestBase {
  test("create statement goal") {
    val lpId = createLearningPath("path 1")

    post(s"/learning-paths/$lpId/draft/goals/",
      s"""{
         |  "goalType": "statement",
         |  "verbId": "http://adlnet.gov/expapi/verbs/experienced",
         |  "objectId": "http://example.com/website",
         |  "objectName": "Example"
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beOk

      body should haveJson(
        """{
          |  "id":1,
          |  "goalType":"statement",
          |  "indexNumber":0,
          |  "optional":false,
          |  "modifiedDate":"2017-02-02T15:08:54Z",
          |  "verbId": "http://adlnet.gov/expapi/verbs/experienced",
          |  "objectId": "http://example.com/website",
          |  "objectName": "Example"
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
         |  "goalType": "statement",
         |  "timeLimit": "P4D",
         |  "verbId": "http://adlnet.gov/expapi/verbs/experienced",
         |  "objectId": "http://example.com/website",
         |  "objectName": "Example"
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beOk

      body should haveJson(
        """{
          |  "id":100,
          |  "groupId":1,
          |  "goalType":"statement",
          |  "indexNumber":0,
          |  "timeLimit": "P4D",
          |  "optional":false,
          |  "modifiedDate":"2017-02-02T15:08:54Z",
          |  "verbId": "http://adlnet.gov/expapi/verbs/experienced",
          |  "objectId": "http://example.com/website",
          |  "objectName": "Example"
          |}
        """.stripMargin,
        ignoreValues = Seq("id", "modifiedDate")
      )
    }
  }

  test("use goal id like group id") {
    val lpId = createLearningPath("path 1")

    val statementGoalId = createStatementGoal(lpId)

    post(s"/goal-groups/$statementGoalId/goals/",
      s"""{
         |  "goalType": "statement",
         |  "verbId": "http://adlnet.gov/expapi/verbs/experienced",
         |  "objectId": "http://example.com/website",
         |  "objectName": "Example"
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beNotFound
    }
  }

  test("update statement goal") {
    val lpId = createLearningPath("path 1")

    val statement1Id = createStatementGoal(lpId)

    put(s"/goals/$statement1Id/",
      s"""{
         |  "goalType": "statement",
         |  "optional": true,
         |  "timeLimit": "P10D"
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beOk

      body should haveJson(
        """{
          |  "id":100,
          |  "goalType":"statement",
          |  "indexNumber":0,
          |  "optional": true,
          |  "timeLimit": "P10D",
          |  "modifiedDate":"2017-02-02T15:08:54Z",
          |  "verbId": "http://adlnet.gov/expapi/verbs/experienced",
          |  "objectId": "http://example.com/website",
          |  "objectName": "Example"
          |}
        """.stripMargin,
        ignoreValues = Seq("id", "modifiedDate")
      )
    }
  }

  test("delete") {
    val lpId = createLearningPath("path 1")

    val statement1Id = createStatementGoal(lpId)

    delete(s"/goals/$statement1Id") {
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
         |  "goalType": "statement",
         |  "verbId": "http://adlnet.gov/expapi/verbs/experienced",
         |  "objectId": "http://example.com/website",
         |  "objectName": "Example"
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
         |  "goalType": "statement",
         |  "verbId": "http://adlnet.gov/expapi/verbs/experienced",
         |  "objectId": "http://example.com/website",
         |  "objectName": "Example"
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beNotAllowed
    }
  }

  test("update published statement goal") {
    val lpId = createLearningPath("path 1")
    val statementId = createStatementGoal(lpId)
    publish(lpId)

    put(s"/goals/$statementId/",
      s"""{ "goalType": "statement", "optional": true }""".stripMargin,
      jsonContentHeaders
    ) {
      status should beNotAllowed
    }
  }

  test("delete published statement goal") {
    val lpId = createLearningPath("path 1")
    val statementId = createStatementGoal(lpId)
    publish(lpId)

    delete(s"/goals/$statementId") {
      status should beNotAllowed
    }
  }
}
