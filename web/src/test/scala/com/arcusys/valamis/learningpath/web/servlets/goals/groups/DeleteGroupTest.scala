package com.arcusys.valamis.learningpath.web.servlets.goals.groups

import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase


class DeleteGroupTest extends LPServletTestBase {

  test("delete group") {
    val lpId = createLearningPath("path 1")

    val group1Id = createGoalGroup(lpId, "group 1")

    delete(s"/goal-groups/$group1Id")(status should beNoContent)

    get(s"/learning-paths/$lpId/draft/goals/tree") {
      status should beOk
      body should be("[]")
    }
  }

  test("delete with wrong id") {
    delete("/goal-groups/135")(status should beNotFound)
  }

  test("delete from published learning path") {

    val lpId = createLearningPath("path 1")
    val groupId = createGoalGroup(lpId, "group 1")
    publish(lpId)

    delete(s"/goal-groups/$groupId")(status should beNotAllowed)
  }

  test("delete not empty group") {
    val lpId = createLearningPath("group to delete")

    val groupId = createGoalGroup(lpId, s"test group")
    for {l1 <- 1 to 10} {
      createLessonGoalInGroup(groupId, lessonId = l1)
      createActivityInGroup(groupId, activityName = "a:"+l1)
      createStatementGoalInGroup(groupId, verbId = "v:"+l1, objectId = "o:"+l1)
      createAssignmentGoalInGroup(groupId, assignmentId = l1)
      createCourseGoalInGroup(groupId, courseId = l1)
      createTrainingEventInGroup(groupId, trainingEventId = l1)
      createWebContentGoalInGroup(groupId, webContentId = l1)

      val group2Id = createSubGoalGroup(groupId, s"g_${l1}_$l1")
      for {l2 <- 1 to 10} {
        createLessonGoalInGroup(group2Id, lessonId = l2)
        createActivityInGroup(group2Id, activityName = "a:"+l2)
        createStatementGoalInGroup(group2Id, verbId = "v:"+l2, objectId = "o:"+l2)
        createAssignmentGoalInGroup(group2Id, assignmentId = l2)
        createCourseGoalInGroup(groupId, courseId = l2)
        createTrainingEventInGroup(group2Id, trainingEventId = l2)
        createWebContentGoalInGroup(group2Id, webContentId = l2)
      }
    }

    val otherGroupId = createGoalGroup(lpId, s"second group")


    delete(s"/goal-groups/$groupId")(status should beNoContent)

    get(s"/learning-paths/$lpId/draft/goals/tree") {
      status should beOk
      body should haveJson(
        s"""[{
          |  "id": $otherGroupId,
          |  "title": "second group",
          |  "goalType": "group"
          |}]""".stripMargin
      )
    }
  }
}
