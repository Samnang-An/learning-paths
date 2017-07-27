package com.arcusys.valamis.learningpath.web.servlets.goals.groups

import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase

/**
  * Created by mminin on 20/02/2017.
  */
class MoveGroupTest extends LPServletTestBase {

  test("move group above") {
    val lpId = createLearningPath("path 1")

    val groupId0 = createGoalGroup(lpId, "g1")
    val groupId1 = createGoalGroup(lpId, "g2")
    val groupId2 = createGoalGroup(lpId, "g3")

    post(s"/goal-groups/$groupId1/move",
      """{ "indexNumber": 0 }""",
      jsonContentHeaders
    ) {
      status should beNoContent
    }

    get(s"/learning-paths/$lpId/draft/goals/tree") {
      status should beOk

      body should haveJson(
        s"""[
          |  {"id":$groupId1, "indexNumber":0 },
          |  {"id":$groupId0, "indexNumber":1 },
          |  {"id":$groupId2, "indexNumber":2 }
          |]
        """.stripMargin)
    }
  }

  test("move group below") {
    val lpId = createLearningPath("path 1")

    val groupId0 = createGoalGroup(lpId, "g1")
    val groupId1 = createGoalGroup(lpId, "g2")
    val groupId2 = createGoalGroup(lpId, "g3")
    val groupId3 = createGoalGroup(lpId, "g4")
    val groupId4 = createGoalGroup(lpId, "g5")
    val groupId5 = createGoalGroup(lpId, "g6")

    post(s"/goal-groups/$groupId0/move",
      """{ "indexNumber": 4 }""",
      jsonContentHeaders
    ) {
      status should beNoContent
    }

    get(s"/learning-paths/$lpId/draft/goals/tree") {
      status should beOk

      body should haveJson(
        s"""[
          |  {"id":$groupId1, "indexNumber":0 },
          |  {"id":$groupId2, "indexNumber":1 },
          |  {"id":$groupId3, "indexNumber":2 },
          |  {"id":$groupId4, "indexNumber":3 },
          |  {"id":$groupId0, "indexNumber":4 },
          |  {"id":$groupId5, "indexNumber":5 }
          |]
        """.stripMargin)
    }
  }

  test("move group to group") {
    val lpId = createLearningPath("path 1")

    val groupId0 = createGoalGroup(lpId, "g1")
    val groupId1 = createGoalGroup(lpId, "g2")
    val groupId2 = createGoalGroup(lpId, "g3")

    val groupId11 = createSubGoalGroup(groupId1, "g3")
    val groupId12 = createSubGoalGroup(groupId1, "g3")
    val groupId13 = createSubGoalGroup(groupId1, "g3")

    post(s"/goal-groups/$groupId0/move",
      s"""{ "groupId": $groupId1, "indexNumber": 2 }""",
      jsonContentHeaders
    ) {
      status should beNoContent
    }

    get(s"/learning-paths/$lpId/draft/goals/tree") {
      status should beOk

      body should haveJson(
        s"""[
           |  {"id":$groupId1, "indexNumber":0, "goals":[
           |    {"id":$groupId11, "groupId":2, "indexNumber":0 },
           |    {"id":$groupId12, "groupId":2, "indexNumber":1 },
           |    {"id":$groupId0,  "groupId":2, "indexNumber":2 },
           |    {"id":$groupId13, "groupId":2, "indexNumber":3 }
           |  ]},
           |  {"id":$groupId2,  "indexNumber":1 }
           |]
        """.stripMargin)
    }
  }

  test("move group to root") {
    val lpId = createLearningPath("path 1")

    val groupId0 = createGoalGroup(lpId, "g1")
    val groupId1 = createGoalGroup(lpId, "g2")
    val groupId2 = createGoalGroup(lpId, "g3")

    val groupId11 = createSubGoalGroup(groupId1, "g3")
    val groupId12 = createSubGoalGroup(groupId1, "g3")
    val groupId13 = createSubGoalGroup(groupId1, "g3")

    post(s"/goal-groups/$groupId12/move",
      s"""{ "indexNumber": 1 }""",
      jsonContentHeaders
    ) {
      status should beNoContent
    }

    get(s"/learning-paths/$lpId/draft/goals/tree") {
      status should beOk

      body should haveJson(
        s"""[
           |  {"id":$groupId0,  "indexNumber":0 },
           |  {"id":$groupId12, "indexNumber":1 },
           |  {"id":$groupId1,  "indexNumber":2, "goals":[
           |    {"id":$groupId11, "groupId":2, "indexNumber":0 },
           |    {"id":$groupId13, "groupId":2, "indexNumber":1 }
           |  ]},
           |  {"id":$groupId2,  "indexNumber":3 }
           |]
        """.stripMargin)
    }
  }

  test("group loop test") {
    val lpId = createLearningPath("path 1")

    val groupId0 = createGoalGroup(lpId, "g1")
    val groupId1 = createSubGoalGroup(groupId0, "g2")
    val groupId2 = createSubGoalGroup(groupId1, "g3")
    val groupId3 = createSubGoalGroup(groupId2, "g4")
    val groupId4 = createSubGoalGroup(groupId3, "g5")
    val groupId5 = createSubGoalGroup(groupId4, "g6")

    post(s"/goal-groups/$groupId1/move",
      s"""{ "groupId":$groupId4, "indexNumber": 1 }""",
      jsonContentHeaders
    ) {
      status should beBadRequest
    }
  }
}
