package com.arcusys.valamis.learningpath.web.servlets

class DeleteLearningPathTest extends LPServletTestBase {

  test("delete by wrong id should return 404") {
    val fakeId = 234

    delete(s"/learning-paths/$fakeId") {
      status should beNotFound
      body should haveJson(s""" { "message": "no learning path with id: $fakeId"} """)
    }
  }

  test("delete by wrong text id should return 404") {
    val fakeId = "cert_1"

    delete(s"/learning-paths/$fakeId") {
      status should beNotFound
      body should haveJson(s""" { "message": "no learning path with id: $fakeId"} """)
    }
  }

  test("delete learning path") {
    val lpId = createLearningPath("path 1")

    delete(s"/learning-paths/$lpId")(status should beNoContent)

    get(s"/learning-paths/$lpId")(status should beNotFound)
  }

  test("delete learning path with logo") {
    val lpId = createLearningPath("path 1")
    val logoUrl = setLogoToDraft(lpId, Array[Byte](1, 3, 4, 5, 6))

    delete(s"/learning-paths/$lpId")(status should beNoContent)

    get(s"/learning-paths/$lpId")(status should beNotFound)

    get("/" + logoUrl)(status should beNotFound)
  }

  test("delete learning path with related competences") {
    val lpId = createLearningPath("path 1")
    addCompetenceToLPDraft(lpId, improvingCompetencesPath, testCompetence(1, 1))
    addCompetenceToLPDraft(lpId, recommendedCompetencesPath, testCompetence(1, 1))
    publish(lpId)
    createNewDraft(lpId)
    addCompetenceToLPDraft(lpId, improvingCompetencesPath, testCompetence(2, 2))
    addCompetenceToLPDraft(lpId, recommendedCompetencesPath, testCompetence(2, 2))
    publish(lpId)
    createNewDraft(lpId)
    addCompetenceToLPDraft(lpId, improvingCompetencesPath, testCompetence(3, 3))
    addCompetenceToLPDraft(lpId, recommendedCompetencesPath, testCompetence(3, 3))

    delete(s"/learning-paths/$lpId")(status should beNoContent)

    get(s"/learning-paths/$lpId")(status should beNotFound)
  }
}
