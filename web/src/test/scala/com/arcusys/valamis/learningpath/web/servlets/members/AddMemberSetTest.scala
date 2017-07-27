package com.arcusys.valamis.learningpath.web.servlets.members

import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase

/**
  * Created by mminin on 02/02/2017.
  */
class AddMemberSetTest extends LPServletTestBase {

  test("add roles") {
    val lpId = createLearningPath("path 1")

    post(s"/learning-paths/$lpId/members/roles/",
      """[ 1, 2 ]""",
      jsonContentHeaders
    ) {
      status should beNoContent
    }
  }

  test("add user groups") {
    val lpId = createLearningPath("path 1")

    post(s"/learning-paths/$lpId/members/groups/",
      """[ 3, 4 ]""",
      jsonContentHeaders
    ) {
      status should beNoContent
    }
  }

  test("add organizations") {
    val lpId = createLearningPath("path 1")

    post(s"/learning-paths/$lpId/members/organizations",
      """[ 5, 6 ]""",
      jsonContentHeaders
    ) {
      status should beNoContent
    }
  }

  test("add user") {
    val lpId = createLearningPath("path 1")

    post(s"/learning-paths/$lpId/members/users",
      """[ 5, 6, 7, 8 ]""",
      jsonContentHeaders
    ) {
      status should beNoContent
    }
  }
}
