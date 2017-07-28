package com.arcusys.valamis.learningpath.web.servlets.competences

import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase

/**
  * Created by pkornilov on 6/14/17.
  */
class CompetencesTest extends LPServletTestBase {

  private val competence1 = testCompetence(1, 11)
  private val competence2 = testCompetence(2, 22)
  private val competence3 = testCompetence(3, 33)
  private val competence4 = testCompetence(4, 44)
  private val competence5 = testCompetence(5, 55)
  private val competence6 = testCompetence(6, 66)

  test("should add and return competences for LP draft") {
    val (lp1Id, _) = createTestData()

    get(s"/learning-paths/$lp1Id/draft/$recommendedCompetencesPath") {
      status should beOk
      body should haveJson(
        s"[$competence1, $competence3, $competence4, $competence5]",
        strict = true
      )
    }

    get(s"/learning-paths/$lp1Id/draft/$improvingCompetencesPath") {
      status should beOk
      body should haveJson(
        s"[$competence1, $competence2, $competence4, $competence6]",
        strict = true
      )
    }

  }

  test("should get competences for LP current version") {
    val (lp1Id, _) = createTestData()

    //getting competences of currentVersion(in our case - after second publish)
    get(s"/learning-paths/$lp1Id/$recommendedCompetencesPath") {
      status should beOk
      body should haveJson(
        s"[$competence1, $competence3, $competence5]",
        strict = true
      )
    }

    get(s"/learning-paths/$lp1Id/$improvingCompetencesPath") {
      status should beOk
      body should haveJson(
        s"[$competence2, $competence4, $competence6]",
        strict = true
      )
    }
  }

  test("should get competences for given LP version") {
    val (_, lp1VersionIdAfterFirstPublish) = createTestData()

    //getting competences of given version
    get(s"/versions/$lp1VersionIdAfterFirstPublish/$recommendedCompetencesPath") {
      status should beOk
      body should haveJson(
        s"[$competence1, $competence3]",
        strict = true
      )
    }

    get(s"/versions/$lp1VersionIdAfterFirstPublish/$improvingCompetencesPath") {
      status should beOk
      body should haveJson(
        s"[$competence2, $competence4]",
        strict = true
      )
    }
  }

  test("should delete competences for LP draft") {
    val (lp1Id, lp1VersionIdAfterFirstPublish) = createTestData()
    delete(s"/learning-paths/$lp1Id/draft/$recommendedCompetencesPath/skills/3") {
      status should beNoContent
    }

    delete(s"/learning-paths/$lp1Id/draft/$improvingCompetencesPath/skills/4") {
      status should beNoContent
    }

    //check, that competences were deleted for last lp draft
    get(s"/learning-paths/$lp1Id/draft/$recommendedCompetencesPath") {
      status should beOk
      body should haveJson(
        s"[$competence1, $competence4, $competence5]",
        strict = true
      )
    }

    get(s"/learning-paths/$lp1Id/draft/$improvingCompetencesPath") {
      status should beOk
      body should haveJson(
        s"[$competence1, $competence2, $competence6]",
        strict = true
      )
    }

    //check, that other versions are not affected
    get(s"/versions/$lp1VersionIdAfterFirstPublish/$recommendedCompetencesPath") {
      status should beOk
      body should haveJson(
        s"[$competence1, $competence3]",
        strict = true
      )
    }

    get(s"/versions/$lp1VersionIdAfterFirstPublish/$improvingCompetencesPath") {
      status should beOk
      body should haveJson(
        s"[$competence2, $competence4]",
        strict = true
      )
    }

    //check, that lp2 competences are not affected
    get(s"/learning-paths/2/draft/$recommendedCompetencesPath") {
      status should beOk
      body should haveJson(
        s"[$competence2, $competence3, $competence6]",
        strict = true
      )
    }

    get(s"/learning-paths/2/draft/$improvingCompetencesPath") {
      status should beOk
      body should haveJson(
        s"[$competence1, $competence4, $competence5]",
        strict = true
      )
    }
  }

  test("should return 'NotFound' if there is no learning path, version or draft (recommended)") {
    checkNotFoundHandling(recommendedCompetencesPath)
  }

  test("should return 'NotFound' if there is no learning path, version or draft (improving)") {
    checkNotFoundHandling(improvingCompetencesPath)
  }

  private def checkNotFoundHandling(path: String): Unit = {
    val lp1Id = createLearningPath("test lp1")
    publish(lp1Id)

    //creating competences
    //no lp
    post(s"/learning-paths/2/draft/$path", competence1, jsonContentHeaders) {
      status should beNotFound
      body should haveJson(
        s"""
           |{
           |  "message":"no learning path with id: 2"
           |}
        """.stripMargin,
        strict = true
      )
    }

    //no draft
    post(s"/learning-paths/$lp1Id/draft/$path", competence1, jsonContentHeaders) {
      status should beNotFound
      body should haveJson(
        s"""
           |{
           |  "message":"learning path with id: 1 has no draft"
           |}
        """.stripMargin,
        strict = true
      )
    }

    //deleting competences
    //no lp
    delete(s"/learning-paths/2/draft/$path/skills/4") {
      status should beNotFound
      body should haveJson(
        s"""
           |{
           |  "message":"no learning path with id: 2"
           |}
        """.stripMargin,
        strict = true
      )
    }

    //no draft
    delete(s"/learning-paths/$lp1Id/draft/$path/skills/4") {
      status should beNotFound
      body should haveJson(
        s"""
           |{
           |  "message":"learning path with id: 1 has no draft"
           |}
        """.stripMargin,
        strict = true
      )
    }

    //getting competences for current version
    //no lp
    get(s"/learning-paths/2/$path") {
      status should beNotFound
      body should haveJson(
        s"""
           |{
           |  "message":"no learning path with id: 2"
           |}
        """.stripMargin,
        strict = true
      )
    }


    //getting competences for draft
    //no lp
    get(s"/learning-paths/2/draft/$path") {
      status should beNotFound
      body should haveJson(
        s"""
           |{
           |  "message":"no learning path with id: 2"
           |}
        """.stripMargin,
        strict = true
      )
    }

    //no draft
    get(s"/learning-paths/$lp1Id/draft/$path") {
      status should beNotFound
      body should haveJson(
        s"""
           |{
           |  "message":"learning path with id: 1 has no draft"
           |}
        """.stripMargin,
        strict = true
      )
    }

    //getting competences for given version
    get(s"/versions/22/$path") {
      status should beNotFound
      body should haveJson(
        s"""
           |{
           |  "message":"no version with id: 22"
           |}
        """.stripMargin,
        strict = true
      )
    }

  }

  test("should return 'BadRequest' if bad skillId while deleting competence") {
    delete(s"/learning-paths/1/draft/$recommendedCompetencesPath/skills/bad") {
      status should beBadRequest
      body should haveJson(
        """
          |{
          | "message":"bad skillId"
          |}
        """.stripMargin,
        strict = true
      )
    }

    delete(s"/learning-paths/1/draft/$improvingCompetencesPath/skills/bad") {
      status should beBadRequest
      body should haveJson(
        """
          |{
          | "message":"bad skillId"
          |}
        """.stripMargin,
        strict = true
      )
    }
  }

  test("should return 'ServerError' when adding duplicated competence") {
    //TODO return not 500 status when adding duplicated competence
    val lp1Id = createLearningPath("test lp 1")
    addCompetenceToLPDraft(lp1Id, recommendedCompetencesPath, competence1)
    post(s"/learning-paths/$lp1Id/draft/$recommendedCompetencesPath", competence1, jsonContentHeaders) {
      status should beCode(500)
    }

    addCompetenceToLPDraft(lp1Id, improvingCompetencesPath, competence1)
    post(s"/learning-paths/$lp1Id/draft/$improvingCompetencesPath", competence1, jsonContentHeaders) {
      status should beCode(500)
    }
  }

  test("should clone competences while cloning LP") {
    val (lpId, _) = createTestData()
    val newLpId = cloneLearningPath(lpId)

    get(s"/learning-paths/$newLpId/$recommendedCompetencesPath") {
      status should beOk
      body should haveJson(
        s"[$competence1, $competence3, $competence5]",
        strict = true
      )
    }

    get(s"/learning-paths/$newLpId/$improvingCompetencesPath") {
      status should beOk
      body should haveJson(
        s"[$competence2, $competence4, $competence6]",
        strict = true
      )
    }

  }

  private def createTestData(): (Long, Long) = {
    //lp1
    val (lp1Id, lp1versionId) = createLearningPathAndGetIds("test lp 1")
    addCompetenceToLPDraft(lp1Id, recommendedCompetencesPath, competence1)
    addCompetenceToLPDraft(lp1Id, recommendedCompetencesPath, competence3)
    addCompetenceToLPDraft(lp1Id, improvingCompetencesPath, competence2)
    addCompetenceToLPDraft(lp1Id, improvingCompetencesPath, competence4)
    publish(lp1Id)

    createNewDraft(lp1Id)
    addCompetenceToLPDraft(lp1Id, recommendedCompetencesPath, competence5)
    addCompetenceToLPDraft(lp1Id, improvingCompetencesPath, competence6)
    publish(lp1Id)

    createNewDraft(lp1Id)
    addCompetenceToLPDraft(lp1Id, recommendedCompetencesPath, competence4)
    addCompetenceToLPDraft(lp1Id, improvingCompetencesPath, competence1)

    //lp2
    val lp2Id = createLearningPath("test lp 2")
    addCompetenceToLPDraft(lp2Id, recommendedCompetencesPath, competence6)
    addCompetenceToLPDraft(lp2Id, improvingCompetencesPath, competence5)
    publish(lp2Id)

    createNewDraft(lp2Id)
    addCompetenceToLPDraft(lp2Id, recommendedCompetencesPath, competence2)
    addCompetenceToLPDraft(lp2Id, improvingCompetencesPath, competence1)
    publish(lp2Id)

    createNewDraft(lp2Id)
    addCompetenceToLPDraft(lp2Id, recommendedCompetencesPath, competence3)
    addCompetenceToLPDraft(lp2Id, improvingCompetencesPath, competence4)
    (lp1Id, lp1versionId)
  }

}
