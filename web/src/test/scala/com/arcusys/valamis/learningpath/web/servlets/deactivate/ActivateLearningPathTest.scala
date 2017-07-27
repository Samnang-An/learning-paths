package com.arcusys.valamis.learningpath.web.servlets.deactivate

import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase

class ActivateLearningPathTest extends LPServletTestBase {

  test("activate not published learning path") {
    val lpId = createLearningPath("test 1")

    deactivate(lpId)

    post(s"/learning-paths/$lpId/activate") {
      status should beNoContent
    }

    get(s"/learning-paths/$lpId") {
      status should beOk

      body should haveJson( s"""{ "id": $lpId, "activated": true }""")
    }
  }

  test("activate with wrong id") {
    post("/learning-paths/1000/activate") {
      status should beNotFound
    }
  }

  test("activate activated learning path") {
    val lpId = createLearningPath("test 1")

    post(s"/learning-paths/$lpId/activate") {
      status should beNoContent
    }

    post(s"/learning-paths/$lpId/activate") {
      status should beNotAllowed
    }
  }
}
