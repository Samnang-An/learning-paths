package com.arcusys.valamis.learningpath.web.servlets.goals

import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase

/**
  * Created by mminin on 20/02/2017.
  */
class MoveGoalTest extends LPServletTestBase {

  test("move goal above") {
    val lpId = createLearningPath("path 1")

    val goalId0 = createActivityGoal(lpId)
    val goalId1 = createActivityGoal(lpId)
    val goalId2 = createActivityGoal(lpId)

    post(s"/goals/$goalId1/move",
      """{ "indexNumber": 0 }""",
      jsonContentHeaders
    ) {
      status should beNoContent
    }

    get(s"/learning-paths/$lpId/draft/goals/tree") {
      status should beOk

      body should haveJson(
        s"""[
          |  {"id":$goalId1, "indexNumber":0 },
          |  {"id":$goalId0, "indexNumber":1 },
          |  {"id":$goalId2, "indexNumber":2 }
          |]
        """.stripMargin)
    }
  }

  test("move goal below") {
    val lpId = createLearningPath("path 1")

    val goalId0 = createActivityGoal(lpId)
    val goalId1 = createActivityGoal(lpId)
    val goalId2 = createActivityGoal(lpId)
    val goalId3 = createActivityGoal(lpId)
    val goalId4 = createActivityGoal(lpId)
    val goalId5 = createActivityGoal(lpId)

    post(s"/goals/$goalId0/move",
      """{ "indexNumber": 4 }""",
      jsonContentHeaders
    ) {
      status should beNoContent
    }

    get(s"/learning-paths/$lpId/draft/goals/tree") {
      status should beOk

      // goal 0 should be on position 4
      body should haveJson(
        s"""[
          |  {"id":$goalId1, "indexNumber":0 },
          |  {"id":$goalId2, "indexNumber":1 },
          |  {"id":$goalId3, "indexNumber":2 },
          |  {"id":$goalId4, "indexNumber":3 },
          |  {"id":$goalId0, "indexNumber":4 },
          |  {"id":$goalId5, "indexNumber":5 }
          |]
        """.stripMargin)
    }
  }

  test("move goal to group") {
    val lpId = createLearningPath("path 1")

    val goalId0 = createActivityGoal(lpId)
    val goalId1 = createActivityGoal(lpId)

    val groupId0 = createGoalGroup(lpId, "g1")

    val goalId01 = createActivityInGroup(groupId0)
    val goalId02 = createActivityInGroup(groupId0)
    val goalId03 = createActivityInGroup(groupId0)

    post(s"/goals/$goalId0/move",
      s"""{ "groupId": $groupId0, "indexNumber": 2 }""",
      jsonContentHeaders
    ) {
      status should beNoContent
    }

    get(s"/learning-paths/$lpId/draft/goals/tree") {
      status should beOk

      body should haveJson(
        s"""[
           |  {"id":$goalId1,  "indexNumber":0, "goalType":"activity" },
           |  {"id":$groupId0, "indexNumber":1, "goalType":"group", "goals":[
           |    {"id":$goalId01, "groupId":3, "indexNumber":0, "goalType":"activity" },
           |    {"id":$goalId02, "groupId":3, "indexNumber":1, "goalType":"activity" },
           |    {"id":$goalId0,  "groupId":3, "indexNumber":2, "goalType":"activity" },
           |    {"id":$goalId03, "groupId":3, "indexNumber":3, "goalType":"activity" }
           |  ]}
           |]
        """.stripMargin)
    }
  }

  test("move goal to root") {
    val lpId = createLearningPath("path 1")

    val goalId0 = createActivityGoal(lpId)
    val goalId1 = createActivityGoal(lpId)

    val groupId = createGoalGroup(lpId, "g1")

    val goalId01 = createActivityInGroup(groupId)
    val goalId02 = createActivityInGroup(groupId)
    val goalId03 = createActivityInGroup(groupId)

    post(s"/goals/$goalId02/move",
      s"""{ "indexNumber": 1 }""",
      jsonContentHeaders
    ) {
      status should beNoContent
    }

    get(s"/learning-paths/$lpId/draft/goals/tree") {
      status should beOk

      body should haveJson(
        s"""[
           |  {"id":$goalId0,  "indexNumber":0, "goalType":"activity" },
           |  {"id":$goalId02, "indexNumber":1, "goalType":"activity" },
           |  {"id":$goalId1,  "indexNumber":2, "goalType":"activity" },
           |  {"id":$groupId,  "indexNumber":3, "goalType":"group", "goals":[
           |    {"id":$goalId01, "groupId":3, "indexNumber":0, "goalType":"activity" },
           |    {"id":$goalId03, "groupId":3, "indexNumber":1, "goalType":"activity" }
           |  ]}
           |]
        """.stripMargin)
    }
  }
}
