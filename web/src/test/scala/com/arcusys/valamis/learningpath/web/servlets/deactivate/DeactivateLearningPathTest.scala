package com.arcusys.valamis.learningpath.web.servlets.deactivate

import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase

class DeactivateLearningPathTest extends LPServletTestBase {

  test("deactivate learning path") {
    val lpId = createLearningPath("test 1")

    publish(lpId)

    post(s"/learning-paths/$lpId/deactivate") {
      status should beNoContent
    }

    get(s"/learning-paths/$lpId") {
      status should beOk

      body should haveJson( s"""{ "id": $lpId, "activated":false }""")
    }
  }

  test("deactivate with wrong id") {
    post("/learning-paths/1000/deactivate") {
      status should beNotFound
    }
  }
}
