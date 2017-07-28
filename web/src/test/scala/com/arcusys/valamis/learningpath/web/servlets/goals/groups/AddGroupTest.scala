package com.arcusys.valamis.learningpath.web.servlets.goals.groups

import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase


class AddGroupTest extends LPServletTestBase {

  test("create root group with minimum data") {
    val lpId = createLearningPath("path 1")

    post(
      uri = s"/learning-paths/$lpId/draft/groups/",
      body = """{ "title": "group #1" }""",
      headers = jsonContentHeaders
    ) {
      status should beOk

      body should haveJson(
        """{
          |  "id": 1,
          |  "title": "group #1",
          |  "indexNumber": 0,
          |  "modifiedDate": "2017-02-02T15:08:54Z"
          |}""".stripMargin,
        ignoreValues = Seq("modifiedDate")
      )
    }
  }

  test("create root group with maximum data") {
    val lpId = createLearningPath("path 1")

    post(s"/learning-paths/$lpId/draft/groups/",
      s"""{
         |  "title": "group #1",
         |  "count": 34,
         |  "timeLimit": "P1D"
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beOk

      body should haveJson(
        """{
          |  "id": 1,
          |  "title": "group #1",
          |  "count": 34,
          |  "indexNumber": 0,
          |  "timeLimit": "P1D",
          |  "modifiedDate": "2017-02-02T15:08:54Z"
          |}
        """.stripMargin,
        ignoreValues = Seq("modifiedDate")
      )
    }
  }

  test("create sub group") {
    val lpId = createLearningPath("path 1")
    val group1Id = createGoalGroup(lpId, "group 1")

    post(s"/goal-groups/$group1Id/groups/",
      s"""{ "title": "group #2" }""",
      jsonContentHeaders
    ) {
      status should beOk

      body should haveJson(
        """{
          |  "id": 100,
          |  "groupId": 1,
          |  "title": "group #2",
          |  "indexNumber": 0,
          |  "modifiedDate": "2017-02-02T15:08:54Z"
          |}
        """.stripMargin,
        ignoreValues = Seq("id", "modifiedDate")
      )
    }
  }

  test("create with wrong id") {
    val lpId = createLearningPath("path 1")
    val group1Id = createGoalGroup(lpId, "group 1")

    post(s"/groups/${group1Id + 1}/groups/",
      s"""{ "title": "group #2" }""",
      jsonContentHeaders
    ) {
      status should beNotFound
    }
  }

  test("create goals in new draft") {
    val lpId = createLearningPath("version 1 title")

    publish(lpId)
    createNewDraft(lpId)
    createGoalGroup(lpId, "group 1")

    get(s"/learning-paths/$lpId/goals/tree") {
      status should beOk

      body should be("[]")
    }
  }

  test("create goals in published group") {
    val lpId = createLearningPath("version 1 title")

    val groupId = createGoalGroup(lpId, "group 1")

    publish(lpId)

    post(s"/goal-groups/$groupId/groups/",
      s"""{ "title": "group #2" }""",
      jsonContentHeaders
    ) {
      status should beNotAllowed
    }
  }

  test("create group should return 404 if no draft exists") {
    val lpId = createLearningPath("version 1 title")

    publish(lpId)

    post(s"/learning-paths/$lpId/draft/groups/",
      s"""{ "title": "group #1" }""",
      jsonContentHeaders
    ) {
      status should beNotFound
    }
  }
}
