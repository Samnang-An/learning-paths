package com.arcusys.valamis.learningpath.web.servlets.goals

import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase


class GoalsTreeByVersionTest extends LPServletTestBase {

  test("get from empty server") {
    get(s"/versions/123/goals/tree") {
      status should beNotFound
      body should be("""{"message":"no version with id: 123"}""")
    }
  }

  test("get with fake id") {
    get(s"/versions/TEST_ID/goals/tree") {
      status should beNotFound
      body should be("""{"message":"no version with id: TEST_ID"}""")
    }
  }


  test("get goals tree by versionId") {
    //create LP with head version
    val (lpId, versionId) = createLearningPathAndGetIds("path 1")

    //fill current version
    val g1Id = createGoalGroup(lpId, "group 1")
    val g1_g1Id = createSubGoalGroup(g1Id, "group 1 > 1")
    val g1_g1_a1Id = createActivityInGroup(g1_g1Id)
    val g1_g1_g1Id = createSubGoalGroup(g1_g1Id, "group 1 > 1")

    val g2Id = createGoalGroup(lpId, "group 2")
    val g2_l1Id = createLessonGoalInGroup(g2Id, lessonId = 123)

    val a1Id = createActivityGoal(lpId)

    //get goals tree by version
    get(s"/versions/$versionId/goals/tree") {
      status should beOk

      body should haveJson(
        s"""[
           |  {
           |    "id": $g1Id,
           |    "optional": false,
           |    "title": "group 1",
           |    "goalType": "group",
           |    "goals": [{
           |        "id": $g1_g1Id,
           |        "groupId": $g1Id,
           |        "optional": false,
           |        "title": "group 1 > 1",
           |        "goalType": "group",
           |        "goals": [
           |          {
           |            "id": $g1_g1_a1Id,
           |            "groupId": $g1_g1Id,
           |            "optional": false,
           |            "activityName": "testActivity",
           |            "goalType": "activity"
           |          }, {
           |            "id": $g1_g1_g1Id,
           |            "groupId": $g1_g1Id,
           |            "optional": false,
           |            "title": "group 1 > 1",
           |            "goals": [],
           |            "goalType": "group"
           |          }
           |        ]
           |      }
           |    ]
           |  }, {
           |    "id": $g2Id,
           |    "optional": false,
           |    "title": "group 2",
           |    "goalType": "group",
           |    "goals": [
           |      {
           |        "id": $g2_l1Id,
           |        "groupId": $g2Id,
           |        "optional": false,
           |        "lessonId": 123,
           |        "title": "Deleted lesson with id 123",
           |        "goalType": "lesson"
           |      }
           |    ]
           |  }, {
           |    "id": $a1Id,
           |    "optional": false,
           |    "activityName": "testActivity",
           |    "goalType": "activity"
           |  }
           |]""".stripMargin
      )
    }
  }

  test("get goals tree by previous version") {
    //create LP with head version
    val (lpId, versionId) = createLearningPathAndGetIds("path 1")

    //fill current version
    val g1Id = createGoalGroup(lpId, "group 1")
    val a1Id = createActivityGoal(lpId)

    //create new version
    publish(lpId)
    createNewDraft(lpId)
    createGoalGroup(lpId, "group 3")
    createGoalGroup(lpId, "group 4")

    //get goals tree by version 1
    get(s"/versions/$versionId/goals/tree") {
      status should beOk

      body should haveJson(
        s"""[
           |  {
           |    "id": $g1Id,
           |    "optional": false,
           |    "title": "group 1",
           |    "goalType": "group",
           |    "goals": []
           |  }, {
           |    "id": $a1Id,
           |    "optional": false,
           |    "activityName": "testActivity",
           |    "goalType": "activity"
           |  }
           |]""".stripMargin
      )
    }
  }
}
