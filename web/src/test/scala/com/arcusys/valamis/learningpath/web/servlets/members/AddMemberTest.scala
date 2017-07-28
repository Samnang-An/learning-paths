package com.arcusys.valamis.learningpath.web.servlets.members

import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase

/**
  * Created by mminin on 02/02/2017.
  */
class AddMemberTest extends LPServletTestBase {

  test("add role") {
    val lpId = createLearningPath("path 1")

    post(s"/learning-paths/$lpId/members/roles",
      "[ 345 ]",
      jsonContentHeaders
    ) {
      status should beNoContent
    }
  }

  test("add user group") {
    val lpId = createLearningPath("path 1")

    post(s"/learning-paths/$lpId/members/groups",
      "[ 345 ]",
      jsonContentHeaders
    ) {
      status should beNoContent
    }
  }

  test("add organization") {
    val lpId = createLearningPath("path 1")

    post(s"/learning-paths/$lpId/members/organizations",
      "[ 345 ]",
      jsonContentHeaders
    ) {
      status should beNoContent
    }
  }

  test("add user") {
    val lpId = createLearningPath("path 1")

    post(s"/learning-paths/$lpId/members/users",
      "[ 345 ]",
      jsonContentHeaders
    ) {
      status should beNoContent
    }
  }
}
