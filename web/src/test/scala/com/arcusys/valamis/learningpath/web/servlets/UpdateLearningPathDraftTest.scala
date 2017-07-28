package com.arcusys.valamis.learningpath.web.servlets

class UpdateLearningPathDraftTest extends LPServletTestBase {

  test("update title") {
    val lpId = createLearningPath("test 1")

    put(s"/learning-paths/$lpId/draft",
      s"""{ "title": "it is my new title" }""",
      jsonContentHeaders
    ) {
      status should beOk

      body should haveJson(
        s"""{
           |  "modifiedDate": "2017-01-31T07:05:52Z"
           |}
         """.stripMargin,
        ignoreValues = Seq("createdDate", "modifiedDate")
      )
    }

    get(s"/learning-paths/$lpId/draft") {
      status should beOk

      body should haveJson(
        s"""{
           |  "id": $lpId,
           |  "activated": false,
           |  "title": "it is my new title",
           |  "createdDate": "2017-01-31T07:05:52Z",
           |  "modifiedDate": "2017-01-31T07:05:52Z"
           |}
         """.stripMargin,
        ignoreValues = Seq("createdDate", "modifiedDate")
      )
    }
  }

  test("update all fields") {
    val lpId = createLearningPath("test 1")

    put(s"/learning-paths/$lpId/draft",
      s"""{
         |  "title": "it is my new title",
         |  "description": "my new description",
         |  "courseId": 555,
         |  "validPeriod": "P1D"
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beOk

      body should haveJson(
        s"""{
           |  "modifiedDate": "2017-01-31T07:05:52Z"
           |}
         """.stripMargin,
        ignoreValues = Seq("createdDate", "modifiedDate")
      )
    }

    get(s"/learning-paths/$lpId/draft") {
      status should beOk

      body should haveJson(
        s"""{
           |  "id": $lpId,
           |  "activated": false,
           |  "title": "it is my new title",
           |  "description": "my new description",
           |  "courseId": 555,
           |  "validPeriod": "P1D",
           |  "createdDate": "2017-01-31T07:05:52Z",
           |  "modifiedDate": "2017-01-31T07:05:52Z"
           |}
         """.stripMargin,
        ignoreValues = Seq("createdDate", "modifiedDate")
      )
    }
  }

  test("reset all fields") {
    val lpId = createLearningPath("test 1")

    put(s"/learning-paths/$lpId/draft",
      s"""{
         |  "title": "it is my new title",
         |  "description": "my new description",
         |  "courseId": 555,
         |  "validPeriod": "P1D"
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beOk

      body should haveJson(
        s"""{
           |  "modifiedDate": "2017-01-31T07:05:52Z"
           |}
         """.stripMargin,
        ignoreValues = Seq("createdDate", "modifiedDate")
      )
    }

    put(s"/learning-paths/$lpId/draft",
      s"""{
         |  "title": "it is my new title"
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beOk
    }

    get(s"/learning-paths/$lpId/draft") {
      status should beOk

      body should haveJson(
        s"""{
           |  "id": $lpId,
           |  "activated": false,
           |  "title": "it is my new title",
           |  "createdDate": "2017-01-31T07:05:52Z",
           |  "modifiedDate": "2017-01-31T07:05:52Z"
           |}
         """.stripMargin,
        ignoreValues = Seq("createdDate", "modifiedDate")
      )
    }
  }

  test("change draft info should not affect on published version") {
    val lpId = createLearningPath("version 1 title")
    publish(lpId)
    createNewDraft(lpId)

    put(s"/learning-paths/$lpId/draft",
      """{ "title": "version 2 (draft) title" }""",
      jsonContentHeaders) {
      status should beOk
    }

    get(s"/learning-paths/$lpId/") {
      status should beOk
      body should haveJson("""{ "title": "version 1 title" }""")
    }

    get(s"/learning-paths/$lpId/draft") {
      status should beOk
      body should haveJson("""{ "title": "version 2 (draft) title" }""")
    }
  }

  test("update should return 404 if no draft exists") {
    val lpId = createLearningPath("version 1 title")
    publish(lpId)

    put(s"/learning-paths/$lpId/draft",
      s"""{ "title": "it is my new title" }""",
      jsonContentHeaders
    ) {
      status should beNotFound
    }
  }
}
