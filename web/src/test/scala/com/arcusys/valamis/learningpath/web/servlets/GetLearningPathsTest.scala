package com.arcusys.valamis.learningpath.web.servlets

import org.json4s._
import org.json4s.jackson.JsonMethods._

class GetLearningPathsTest extends LPServletTestBase {

  test("get /learning-paths from empty server") {

    get("/learning-paths") {
      status should beOk
      body should haveJson("""{ "items":[], "total":0}""")
    }
  }

  test("get /learning-paths should return json array with learning paths") {
    val lp1Id = createLearningPath("path 1")
    val lp2Id = createLearningPath("path 2")
    val lp3Id = createLearningPath("path 3", courseId = Some(123))

    get("/learning-paths") {
      status should beOk

      body should haveJson(
        s"""{
           | "total": 3,
           | "items":[{
           |   "id":$lp1Id,
           |   "activated":false,
           |   "title":"path 1",
           |   "createdDate":"2017-01-31T11:23:18Z",
           |   "modifiedDate":"2017-01-31T11:23:18Z"
           |  },{
           |   "id":$lp2Id,
           |   "activated":false,
           |   "title":"path 2",
           |   "createdDate":"2017-01-31T11:23:18Z",
           |   "modifiedDate":"2017-01-31T11:23:18Z"
           |  },{
           |   "id":$lp3Id,
           |   "activated":false,
           |   "title":"path 3",
           |   "courseId": 123,
           |   "createdDate":"2017-01-31T11:23:18Z",
           |   "modifiedDate":"2017-01-31T11:23:18Z"
           |  }]
           |}""".stripMargin,
        ignoreValues = Seq("createdDate", "modifiedDate")
      )
    }
  }


  test("get /learning-paths?filter=ath should return json array with filtered learning paths") {
    val lp1Id = createLearningPath("test 1")
    val lp2Id = createLearningPath("PATH 2")
    val lp3Id = createLearningPath("pAtH 3", courseId = Some(123))

    get("/learning-paths?title=ath") {
      status should beOk

      body should haveJson(
        s"""{
           | "total": 2,
           | "items":[{
           |   "id":$lp2Id,
           |   "activated":false,
           |   "title":"PATH 2",
           |   "createdDate":"2017-01-31T11:23:18Z",
           |   "modifiedDate":"2017-01-31T11:23:18Z"
           |  },{
           |   "id":$lp3Id,
           |   "activated":false,
           |   "title":"pAtH 3",
           |   "courseId": 123,
           |   "createdDate":"2017-01-31T11:23:18Z",
           |   "modifiedDate":"2017-01-31T11:23:18Z"
           |  }]
           |}""".stripMargin,
        ignoreValues = Seq("createdDate", "modifiedDate")
      )
    }
  }

  test("get /learning-paths?courseId=23 should return elements from course only") {
    val courseId = 23

    val lp1Id = createLearningPath("path 1")
    val lp2Id = createLearningPath("path 2", Some(courseId))
    val lp3Id = createLearningPath("path 3", Some(courseId))

    get("/learning-paths?courseId=23") {
      status should beOk

      body should haveJson(
        s"""{
           |  "total": 2,
           |  "items": [{
           |    "id":$lp2Id,
           |    "activated":false,
           |    "title":"path 2",
           |    "courseId": 23,
           |    "createdDate":"2017-01-31T11:23:18Z",
           |    "modifiedDate":"2017-01-31T11:23:18Z"
           |  },{
           |    "id":$lp3Id,
           |    "activated":false,
           |    "title":"path 3",
           |    "courseId": 23,
           |    "createdDate":"2017-01-31T11:23:18Z",
           |    "modifiedDate":"2017-01-31T11:23:18Z"
           |  }]
           |}""".stripMargin,
        ignoreValues = Seq("createdDate", "modifiedDate")
      )
    }
  }

  test("get /learning-paths?title=test") {
    val courseId = 23

    val lp1Id = createLearningPath("test 1")
    val lp2Id = createLearningPath("text 2", Some(courseId))
    val lp3Id = createLearningPath("path 3", Some(courseId))

    get("/learning-paths?title=test") {
      status should beOk

      body should haveJson(
        s"""{
           |  "total": 1,
           |  "items": [{
           |    "id":$lp1Id,
           |    "activated":false,
           |    "title":"test 1",
           |    "createdDate":"2017-01-31T11:23:18Z",
           |    "modifiedDate":"2017-01-31T11:23:18Z"
           |  }]
           |}""".stripMargin,
        ignoreValues = Seq("createdDate", "modifiedDate")
      )
    }
  }

  test("get /learning-paths?skip=1&take=2&sort=createdDate") {
    val courseId = 23

    val lp1Id = createLearningPath("test 1")
    val lp2Id = createLearningPath("text 2", Some(courseId))
    val lp3Id = createLearningPath("path 3", Some(courseId))

    get("/learning-paths?skip=1&take=2&sort=createdDate") {
      status should beOk

      body should haveJson(
        s"""{
           |  "total": 3,
           |  "items": [{
           |    "id":$lp2Id,
           |    "activated":false,
           |    "title":"text 2",
           |    "courseId": 23,
           |    "createdDate":"2017-01-31T11:23:18Z",
           |    "modifiedDate":"2017-01-31T11:23:18Z"
           |  },{
           |    "id":$lp3Id,
           |    "activated":false,
           |    "title":"path 3",
           |    "courseId": 23,
           |    "createdDate":"2017-01-31T11:23:18Z",
           |    "modifiedDate":"2017-01-31T11:23:18Z"
           |  }]
           |}""".stripMargin,
        ignoreValues = Seq("createdDate", "modifiedDate")
      )
    }
  }

  test("get /learning-paths?skip=1&take=2&sort=-title") {
    val courseId = 23

    val lp1Id = createLearningPath("a")
    val lp2Id = createLearningPath("z", Some(courseId))
    val lp3Id = createLearningPath("m", Some(courseId))

    get("/learning-paths?skip=1&take=2&sort=-title") {
      status should beOk

      body should haveJson(
        s"""{
           |  "total": 3,
           |  "items": [{
           |    "id":$lp3Id,
           |    "activated":false,
           |    "title":"m",
           |    "courseId": 23,
           |    "createdDate":"2017-01-31T11:23:18Z",
           |    "modifiedDate":"2017-01-31T11:23:18Z"
           |  },{
           |    "id":$lp1Id,
           |    "activated":false,
           |    "title":"a",
           |    "createdDate":"2017-01-31T11:23:18Z",
           |    "modifiedDate":"2017-01-31T11:23:18Z"
           |  }]
           |}""".stripMargin,
        ignoreValues = Seq("createdDate", "modifiedDate")
      )
    }
  }

  test("get /learning-paths?skip=20&take=2&sort=-createdDate") {
    for { i <- 1 to 100} createLearningPath("item " + i)

    get("/learning-paths?skip=20&take=2&sort=-createdDate") {
      status should beOk

      body should haveJson(
        s"""{
           |  "total": 100,
           |  "items": [{
           |    "id":80,
           |    "activated":false,
           |    "title":"item 80",
           |    "createdDate":"2017-01-31T11:23:18Z",
           |    "modifiedDate":"2017-01-31T11:23:18Z"
           |  },{
           |    "id":79,
           |    "activated":false,
           |    "title":"item 79",
           |    "createdDate":"2017-01-31T11:23:18Z",
           |    "modifiedDate":"2017-01-31T11:23:18Z"
           |  }]
           |}""".stripMargin,
        ignoreValues = Seq("createdDate", "modifiedDate", "id", "title")
      )
    }
  }

  test("get /learning-paths without skip take should return first 10 items") {
    for { i <- 1 to 100} createLearningPath("item " + i)

    get("/learning-paths") {
      status should beOk

      body should haveJson(
        s"""{
           |  "total": 100,
           |  "items": []
           |}""".stripMargin,
        ignoreValues = Seq("items")
      )

      (parse(body) \ "items").children should have size 10
    }
  }

  test("get /learning-paths by userId") {
    for {i <- 1 to 100} createLearningPath("item " + i, headers = Map("userId" -> "14"))
    for {i <- 1 to 123} createLearningPath("user 2 item " + i, headers = Map("userId" -> "2"))

    get("/learning-paths?userId=2&take=2&sort=createdDate") {
      status should beOk

      body should haveJson(
        s"""{
           |  "total": 123,
           |  "items": [{
           |    "id":101,
           |    "activated":false,
           |    "title":"user 2 item 1",
           |    "openBadgesEnabled":false,
           |    "createdDate":"2017-02-17T13:42:32Z",
           |    "modifiedDate":"2017-02-17T13:42:32Z"
           |  },{
           |    "id":102,
           |    "activated":false,
           |    "title":"user 2 item 2",
           |    "openBadgesEnabled":false,
           |    "createdDate":"2017-02-17T13:42:32Z",
           |    "modifiedDate":"2017-02-17T13:42:32Z"
           |  }]
           |}""".stripMargin,
        ignoreValues = Seq("id", "createdDate", "modifiedDate")
      )
    }
  }
}
