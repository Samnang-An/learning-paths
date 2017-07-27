package com.arcusys.valamis.learningpath.web.servlets.goals

import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase

/**
  * Created by mminin on 20/02/2017.
  */
class GoalsTreeTest extends LPServletTestBase {

  test("get from empty server") {
    get(s"/learning-paths/123/goals/tree") {
      status should beNotFound
    }
  }

  test("get draft tree from empty server") {
    get(s"/learning-paths/123/draft/goals/tree") {
      status should beNotFound
    }
  }

  test("get from empty learning path") {
    val lpId = createLearningPath("path 1")

    get(s"/learning-paths/$lpId/draft/goals/tree") {
      status should beOk
      body should be("[]")
    }
  }

  test("get goals tree") {
    val lpId = createLearningPath("path 1")

    val g1Id = createGoalGroup(lpId, "group 1")
    val g1_g1Id = createSubGoalGroup(g1Id, "group 1 > 1")
    val g1_g1_l1Id = createLessonGoalInGroup(g1_g1Id, lessonId = 11)
    val g1_g1_a1Id = createActivityInGroup(g1_g1Id)
    val g1_g1_g1Id = createSubGoalGroup(g1_g1Id, "group 1 > 1")

    val g2Id = createGoalGroup(lpId, "group 2")
    val g2_l1Id = createLessonGoalInGroup(g2Id, lessonId = 123)
    val g2_l2Id = createLessonGoalInGroup(g2Id, lessonId = 234)
    val g2_l3Id = createLessonGoalInGroup(g2Id, lessonId = 345)

    val a1Id = createActivityGoal(lpId)

    get(s"/learning-paths/$lpId/draft/goals/tree") {
      status should beOk

      body should haveJson(
        s"""[
           |  {
           |    "id": $g1Id,
           |    "indexNumber": 0,
           |    "optional": false,
           |    "modifiedDate": "2017-02-21T09:28:56Z",
           |    "title": "group 1",
           |    "goalType": "group",
           |    "goals": [{
           |        "id": $g1_g1Id,
           |        "groupId": $g1Id,
           |        "indexNumber": 0,
           |        "optional": false,
           |        "modifiedDate": "2017-02-21T09:28:56Z",
           |        "title": "group 1 > 1",
           |        "goalType": "group",
           |        "goals": [
           |          {
           |            "id": $g1_g1_l1Id,
           |            "groupId": $g1_g1Id,
           |            "indexNumber": 0,
           |            "optional": false,
           |            "modifiedDate": "2017-02-21T09:28:56Z",
           |            "lessonId": 11,
           |            "title": "Deleted lesson with id 11",
           |            "goalType": "lesson"
           |          }, {
           |            "id": $g1_g1_a1Id,
           |            "groupId": $g1_g1Id,
           |            "indexNumber": 1,
           |            "optional": false,
           |            "modifiedDate": "2017-02-21T09:28:56Z",
           |            "activityName": "testActivity",
           |            "goalType": "activity"
           |          }, {
           |            "id": $g1_g1_g1Id,
           |            "groupId": $g1_g1Id,
           |            "indexNumber": 2,
           |            "optional": false,
           |            "modifiedDate": "2017-02-21T09:28:56Z",
           |            "title": "group 1 > 1",
           |            "goals": [],
           |            "goalType": "group"
           |          }
           |        ]
           |      }
           |    ]
           |  }, {
           |    "id": $g2Id,
           |    "indexNumber": 1,
           |    "optional": false,
           |    "modifiedDate": "2017-02-21T09:28:56Z",
           |    "title": "group 2",
           |    "goalType": "group",
           |    "goals": [
           |      {
           |        "id": $g2_l1Id,
           |        "groupId": $g2Id,
           |        "indexNumber": 0,
           |        "optional": false,
           |        "modifiedDate": "2017-02-21T09:28:56Z",
           |        "lessonId": 123,
           |        "title": "Deleted lesson with id 123",
           |        "goalType": "lesson"
           |      }, {
           |        "id": $g2_l2Id,
           |        "groupId": $g2Id,
           |        "indexNumber": 1,
           |        "optional": false,
           |        "modifiedDate": "2017-02-21T09:28:56Z",
           |        "lessonId": 234,
           |        "title": "Deleted lesson with id 234",
           |        "goalType": "lesson"
           |      }, {
           |        "id": $g2_l3Id,
           |        "groupId": $g2Id,
           |        "indexNumber": 2,
           |        "optional": false,
           |        "modifiedDate": "2017-02-21T09:28:56Z",
           |        "lessonId": 345,
           |        "title": "Deleted lesson with id 345",
           |        "goalType": "lesson"
           |      }
           |    ]
           |  }, {
           |    "id": $a1Id,
           |    "indexNumber": 2,
           |    "optional": false,
           |    "modifiedDate": "2017-02-21T09:28:56Z",
           |    "activityName": "testActivity",
           |    "goalType": "activity"
           |  }
           |]""".stripMargin,
        ignoreValues = Seq("modifiedDate")
      )
    }
  }
}
