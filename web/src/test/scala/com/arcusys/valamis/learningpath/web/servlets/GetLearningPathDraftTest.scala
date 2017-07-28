package com.arcusys.valamis.learningpath.web.servlets

class GetLearningPathDraftTest  extends LPServletTestBase {

  test("get by wrong id should return 404") {
    val fakeId = 234

    get(s"/learning-paths/$fakeId/draft") {
      status should beNotFound
      body should haveJson(s""" { "message": "no learning path with id: $fakeId"} """)
    }
  }

  test("get by wrong text id should return 404") {
    val fakeId = "cert_1"

    get(s"/learning-paths/$fakeId/draft") {
      status should beNotFound
      body should haveJson(s""" { "message": "no learning path with id: $fakeId"} """)
    }
  }

  test("get from LP without draft") {
    val lpId = createLearningPath("test")

    publish(lpId)

    get(s"/learning-paths/$lpId/draft") {
      status should beNotFound
      body should haveJson(s""" {"message":"learning path with id: $lpId has no draft"} """)
    }
  }

  test("get by id should return existed learning path") {
    val lp1Id = createLearningPath("path 1")
    val lp2Id = createLearningPath("path 2")
    val lp3Id = createLearningPath("path 3")

    get(s"/learning-paths/$lp2Id/draft") {
      status should beOk

      body should haveJson(
        s"""{
           |  "id": $lp2Id,
           |  "activated": false,
           |  "title": "path 2",
           |  "createdDate": "2017-01-31T07:05:52Z",
           |  "modifiedDate": "2017-01-31T07:05:52Z"
           |}
         """.stripMargin,
        ignoreValues = Seq("createdDate", "modifiedDate")
      )
    }
  }
}
