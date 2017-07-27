package com.arcusys.valamis.learningpath.web.servlets.webcontent

import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase

class PostWebContentsTest extends LPServletTestBase {

  test("create web content goal") {
    val lpId = createLearningPath("path 1")

    post(s"/learning-paths/$lpId/draft/goals/",
      s"""{
         |  "goalType": "webContent",
         |  "webContentId": 500
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beOk

      body should haveJson(
        """{
          |  "id":1,
          |  "goalType":"webContent",
          |  "indexNumber":0,
          |  "optional":false,
          |  "modifiedDate":"2017-02-02T15:08:54Z",
          |  "webContentId": 500,
          |  "title": "Deleted webContent with id 500"
          |}
        """.stripMargin,
        ignoreValues = Seq("modifiedDate")
      )
    }
  }


  test("create web content goal in group") {
    val lpId = createLearningPath("path 1")

    val group1Id = createGoalGroup(lpId, "group 1")
    val webContentId = 11L

    post(s"/goal-groups/$group1Id/goals/",
      s"""{
         |  "goalType": "webContent",
         |  "webContentId": $webContentId,
         |  "timeLimit": "P4D"
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beOk

      body should haveJson(
        s"""{
           |  "id":100,
           |  "groupId":1,
           |  "goalType":"webContent",
           |  "indexNumber":0,
           |  "timeLimit": "P4D",
           |  "optional":false,
           |  "modifiedDate":"2017-02-02T15:08:54Z",
           |  "webContentId": $webContentId,
           |  "title": "Deleted webContent with id 11"
           |}
        """.stripMargin,
        ignoreValues = Seq("id", "modifiedDate")
      )
    }
  }

}
