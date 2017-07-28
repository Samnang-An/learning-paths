package com.arcusys.valamis.learningpath.web.servlets

class AddLearningPathTest extends LPServletTestBase {

  test("should be enough title to create new learning path") {
    post("/learning-paths",
      """{ "title": "lp 1" }""",
      jsonContentHeaders
    ) {
      status should beOk

      body should haveJson(
        s"""{
           |  "id": 1,
           |  "activated": false,
           |  "title": "lp 1",
           |  "createdDate": "2017-01-31T07:05:52Z",
           |  "modifiedDate": "2017-01-31T07:05:52Z"
           |}
         """.stripMargin,
        ignoreValues = Seq("createdDate", "modifiedDate")
      )
    }
  }

  test("should be possible to create learning path with additional info") {
    post("/learning-paths",
      """{
        |  "title": "lp 2",
        |  "description": "test path",
        |  "courseId": 23,
        |  "validPeriod": "P5M",
        |  "expiringPeriod": "P10D",
        |  "openBadgesEnabled": true,
        |  "openBadgesDescription": "obd"
        |} """.stripMargin,
      jsonContentHeaders
    ) {
      status should beOk

      body should haveJson(
        s"""{
           |  "id": 1,
           |  "activated": false,
           |  "title": "lp 2",
           |  "description": "test path",
           |  "courseId": 23,
           |  "validPeriod": "P5M",
           |  "expiringPeriod": "P10D",
           |  "openBadgesEnabled": true,
           |  "openBadgesDescription": "obd",
           |  "createdDate": "2017-01-31T07:05:52Z",
           |  "modifiedDate": "2017-01-31T07:05:52Z"
           |}""".stripMargin,
        ignoreValues = Seq("createdDate", "modifiedDate")
      )
    }
  }
}
