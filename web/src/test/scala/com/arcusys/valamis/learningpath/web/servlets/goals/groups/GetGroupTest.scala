package com.arcusys.valamis.learningpath.web.servlets.goals.groups

import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase


class GetGroupTest extends LPServletTestBase {

  test("get group") {
    val lpId = createLearningPath("path 1")

    val group1Id = createGoalGroup(lpId, "group 1")

    get(s"/goal-groups/$group1Id"){
      status should beOk

      body should haveJson(
        """{
          |  "id": 1,
          |  "title": "group 1",
          |  "indexNumber": 0,
          |  "modifiedDate": "2017-02-02T15:08:54Z"
          |}""".stripMargin,
        ignoreValues = Seq("modifiedDate")
      )
    }
  }

  test("get with wrong id") {
    get(s"/goal-groups/135")(status should beNotFound)
  }
}
