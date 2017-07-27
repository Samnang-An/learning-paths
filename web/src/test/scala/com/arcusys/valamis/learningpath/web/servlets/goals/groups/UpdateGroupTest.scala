package com.arcusys.valamis.learningpath.web.servlets.goals.groups

import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase


class UpdateGroupTest extends LPServletTestBase {

  test("update group") {
    val lpId = createLearningPath("path 1")

    val group1Id = createGoalGroup(lpId, "group 1")

    put(s"/goal-groups/$group1Id",
      s"""{
         |  "title": "group 1+",
         |  "count": 34,
         |  "timeLimit": "P1D"
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beOk

      body should haveJson(
        """{
          |  "id": 1,
          |  "title": "group 1+",
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

  test("update with wrong id") {
    put(s"/goal-groups/135",
      s"""{
         |  "title": "group 1+",
         |  "count": 34,
         |  "indexNumber": 0,
         |  "timeLimit": "P1D"
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beNotFound
    }
  }

  test("update published group") {

    val lpId = createLearningPath("path 1")
    val groupId = createGoalGroup(lpId, "group 1")
    publish(lpId)

    put(s"/goal-groups/$groupId",
      s"""{ "title": "group 1+" }""",
      jsonContentHeaders
    ) {
      status should beNotAllowed
    }
  }
}
