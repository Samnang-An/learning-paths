package com.arcusys.valamis.learningpath.web.servlets

/**
  * Created by pkornilov on 6/1/17.
  */
class GetLearningPathsOfAGroupForCompetencesTest extends GetLearningPathsForCompetencesBase {

  test("should return 'Bad request' if bad group id") {
    createTestData()

    get(s"groups/badId/learning-paths?groupType=role") {
      status should beBadRequest
      body should haveJson(
        s"""
           |{
           | "message": "bad group id"
           |}
        """.stripMargin
      )
    }

  }

  test("should return 'Bad request' if groupType is missing") {
    createTestData()

    get(s"groups/${role1.id}/learning-paths") {
      status should beBadRequest
      body should haveJson(
        s"""
           |{
           | "message": "missing groupType"
           |}
        """.stripMargin
      )
    }

  }

  test("should return 'Bad request' for bad groupType") {
    createTestData()

    get(s"groups/${role1.id}/learning-paths?groupType=user") {
      status should beBadRequest
      body should haveJson(
        s"""
           |{
           | "message": "wrong groupType value: user"
           |}
        """.stripMargin
      )
    }

  }

  test("should return 'Bad request' if skip without take") {
    get(s"groups/${userGroup1.id}/learning-paths?groupType=group&skip=1") {
      status should beBadRequest
      body should haveJson(
        s"""
           |{
           |  "message": "skip and take should be used together"
           |}
        """.stripMargin,
        strict = true
      )
    }

    get(s"groups/${userGroup1.id}/learning-paths?groupType=group&take=1") {
      status should beBadRequest
      body should haveJson(
        s"""
           |{
           |  "message": "skip and take should be used together"
           |}
        """.stripMargin,
        strict = true
      )
    }
  }

  test("should return 'Not found' for nonexistent userGroup/role/organization ") {
    createTestData()

    val badId = 333L

    get(s"groups/333/learning-paths?groupType=group") {
      status should beNotFound
      body should haveJson(
        s"""
           |{
           | "message": "There are no group with id $badId"
           |}
        """.stripMargin
      )
    }

    get(s"groups/333/learning-paths?groupType=role") {
      status should beNotFound
      body should haveJson(
        s"""
           |{
           | "message": "There are no role with id $badId"
           |}
        """.stripMargin
      )
    }

    get(s"groups/333/learning-paths?groupType=organization") {
      status should beNotFound
      body should haveJson(
        s"""
           |{
           | "message": "There are no organization with id $badId"
           |}
        """.stripMargin
      )
    }

  }

  test("should return learning paths with succeeded user count for an user group sorted by modifiedDate") {
    val lp2Logo = createTestData()

    get(s"groups/${userGroup1.id}/learning-paths?groupType=group") {
      status should beOk
      body should haveJson(
        s"""
           |[
           |  {
           |    "id": 3,
           |    "title": "other lp1",
           |    "shortDescription": "",
           |    "description": "",
           |    "logo": "",
           |    "succeededUserCount": 1
           |  },
           |  {
           |    "id": 2,
           |    "title": "lp2",
           |    "shortDescription": "obd",
           |    "description": "lp2 desc",
           |    "logo": "learning-paths/$lp2Logo",
           |    "succeededUserCount": 1
           |  },
           |  {
           |    "id": 1,
           |    "title": "lp1",
           |    "shortDescription": "",
           |    "description": "",
           |    "logo": "",
           |    "succeededUserCount": 1
           |  }
           |]
        """.stripMargin,
        strict = true
      )
    }
  }

  test("should return learning paths with succeeded user count for a role sorted by modifiedDate") {
    val lp2Logo = createTestData()

    get(s"groups/${role1.id}/learning-paths?groupType=role") {
      status should beOk
      body should haveJson(
        s"""
           |[
           |  {
           |    "id": 3,
           |    "title": "other lp1",
           |    "shortDescription": "",
           |    "description": "",
           |    "logo": "",
           |    "succeededUserCount": 1
           |  },
           |  {
           |    "id": 2,
           |    "title": "lp2",
           |    "shortDescription": "obd",
           |    "description": "lp2 desc",
           |    "logo": "learning-paths/$lp2Logo",
           |    "succeededUserCount": 1
           |  },
           |  {
           |    "id": 1,
           |    "title": "lp1",
           |    "shortDescription": "",
           |    "description": "",
           |    "logo": "",
           |    "succeededUserCount": 2
           |  }
           |]
        """.stripMargin,
        strict = true
      )
    }
  }

  test("should return learning paths with succeeded user count for an organization sorted by modifiedDate") {
    val lp2Logo = createTestData()

    get(s"groups/${organization1.id}/learning-paths?groupType=organization") {
      status should beOk
      body should haveJson(
        s"""
           |[
           |  {
           |    "id": 2,
           |    "title": "lp2",
           |    "shortDescription": "obd",
           |    "description": "lp2 desc",
           |    "logo": "learning-paths/$lp2Logo",
           |    "succeededUserCount": 2
           |  },
           |  {
           |    "id": 1,
           |    "title": "lp1",
           |    "shortDescription": "",
           |    "description": "",
           |    "logo": "",
           |    "succeededUserCount": 1
           |  }
           |]
           |
        """.stripMargin,
        strict = true
      )
    }
  }

  test("should correctly handle skip take") {
    val lp2Logo = createTestData()

    get(s"groups/${userGroup1.id}/learning-paths?groupType=group&skip=1&take=1") {
      status should beOk
      body should haveJson(
        s"""
           |[
           |  {
           |    "id": 2,
           |    "title": "lp2",
           |    "shortDescription": "obd",
           |    "description": "lp2 desc",
           |    "logo": "learning-paths/$lp2Logo",
           |    "succeededUserCount": 1
           |  }
           |]
        """.stripMargin,
        strict = true
      )
    }
  }

}
