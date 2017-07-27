package com.arcusys.valamis.learningpath.web.servlets

/**
  * Created by pkornilov on 5/31/17.
  */
class GetLearningPathsForCompetencesTest extends GetLearningPathsForCompetencesBase {

  test("should return 'Bad request' if 'title' param is missing") {
    get("competences-certificates") {
      status should beBadRequest
      body should haveJson(
        """
          |{
          | "message": "'title' is missing"
          |}
        """.stripMargin)
    }
  }

  test("should return all learning paths with succeeded users") {
    val lp2Logo = createTestData()

    get("competences-certificates?title=") {
      status should beOk
      body should haveJson(
        s"""
           |[
           |  {
           |    "id": 1,
           |    "title": "lp1",
           |    "shortDescription": "",
           |    "description": "",
           |    "logo": "",
           |    "succeededUsers": [
           |      {
           |        "id": 102,
           |        "name": "user 2"
           |      },
           |      {
           |        "id": 103,
           |        "name": "user 3"
           |      }
           |    ]
           |  },
           |  {
           |    "id": 2,
           |    "title": "lp2",
           |    "shortDescription": "obd",
           |    "description": "lp2 desc",
           |    "logo": "learning-paths/$lp2Logo",
           |    "succeededUsers": [
           |      {
           |        "id": 101,
           |        "name": "user 1"
           |      },
           |      {
           |        "id": 103,
           |        "name": "user 3"
           |      }
           |    ]
           |  },
           |  {
           |    "id": 3,
           |    "title": "other lp1",
           |    "shortDescription": "",
           |    "description": "",
           |    "logo": "",
           |    "succeededUsers": [
           |      {
           |        "id": 102,
           |        "name": "user 2"
           |      }
           |    ]
           |  }
           |]
        """.stripMargin,
        strict = true
      )
    }
  }

  test("should return learning paths with succeeded users with filter by title") {
    createTestData()

    get("competences-certificates?title=other") {
      status should beOk
      body should haveJson(
        """
          |[
          |  {
          |    "id": 3,
          |    "title": "other lp1",
          |    "shortDescription": "",
          |    "description": "",
          |    "logo": "",
          |    "succeededUsers": [
          |      {
          |        "id": 102,
          |        "name": "user 2"
          |      }
          |    ]
          |  }
          |]
        """.stripMargin,
        strict = true
      )
    }
  }

  test("should return given amount of learning paths with succeeded users ") {
    val lp2Logo = createTestData()

    get("competences-certificates?title=&count=2") {
      status should beOk
      body should haveJson(
        s"""
           |[
           |  {
           |    "id": 1,
           |    "title": "lp1",
           |    "shortDescription": "",
           |    "description": "",
           |    "logo": "",
           |    "succeededUsers": [
           |      {
           |        "id": 102,
           |        "name": "user 2"
           |      },
           |      {
           |        "id": 103,
           |        "name": "user 3"
           |      }
           |    ]
           |  },
           |  {
           |    "id": 2,
           |    "title": "lp2",
           |    "shortDescription": "obd",
           |    "description": "lp2 desc",
           |    "logo": "learning-paths/$lp2Logo",
           |    "succeededUsers": [
           |      {
           |        "id": 101,
           |        "name": "user 1"
           |      },
           |      {
           |        "id": 103,
           |        "name": "user 3"
           |      }
           |    ]
           |  }
           |]
        """.stripMargin,
        strict = true
      )
    }
  }

}
