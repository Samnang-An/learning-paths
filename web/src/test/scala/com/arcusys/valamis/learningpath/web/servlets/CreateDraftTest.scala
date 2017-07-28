package com.arcusys.valamis.learningpath.web.servlets

/**
  * Created by mminin on 02/02/2017.
  */
class CreateDraftTest extends LPServletTestBase {

  test("new learning path should have draft version") {
    val lpId = createLearningPath("version 1 title")

    get(s"/learning-paths/$lpId/draft") {
      status should beOk

      body should haveJson(
        s"""{
           |  "id":$lpId,
           |  "published":false,
           |  "title":"version 1 title",
           |  "openBadgesEnabled":false,
           |  "createdDate":"2017-03-06T15:30:52Z",
           |  "modifiedDate":"2017-03-06T15:30:52Z"
           |}""".stripMargin,
        ignoreValues = Seq("createdDate", "modifiedDate")
      )
    }
  }


  test("should be possible to create new draft") {
    val lpId = createLearningPath("version 1 title")

    publish(lpId)

    post(s"/learning-paths/$lpId/draft") {
      status should beOk

      body should haveJson(
        s"""{
           |  "id":$lpId,
           |  "published":false,
           |  "title":"version 1 title",
           |  "openBadgesEnabled":false,
           |  "createdDate":"2017-03-06T15:30:52Z",
           |  "modifiedDate":"2017-03-06T15:30:52Z"
           |}""".stripMargin,
        ignoreValues = Seq("createdDate", "modifiedDate")
      )
    }
  }

  test("hasDraft should be true after new draft creation" ) {

    val lpId = createLearningPath("version 1 title")

    publish(lpId)
    createNewDraft(lpId)

    get(s"/learning-paths/$lpId") {
      status should beOk

      body should haveJson(
        s"""{
           |  "id":$lpId,
           |  "published": true,
           |  "hasDraft": true,
           |  "title":"version 1 title"
           |}""".stripMargin)
    }

    get(s"/learning-paths/$lpId/draft") {
      status should beOk

      body should haveJson(
        s"""{
           |  "id":$lpId,
           |  "published":false,
           |  "title":"version 1 title"
           |}""".stripMargin)
    }
  }

  test("create new draft from huge learning path") {
    val lpId = createLearningPath("version 1 title")
    for {l1 <- 1 to 10} {
      createLessonGoal(lpId, lessonId = 23)
      createActivityGoal(lpId, activityName = "a:"+l1)
      createStatementGoal(lpId, verbId = "v:"+l1, objectId = "o:"+l1)
      createAssignmentGoal(lpId, assignmentId = l1)
      createCourseGoal(lpId, courseId = l1)
      createTrainingEventGoal(lpId, trainingEventId = l1)
      createWebContentGoal(lpId, webContentId = l1)

      val groupId = createGoalGroup(lpId, s"g_$l1")
      for {l2 <- 1 to 10} {
        createLessonGoalInGroup(groupId, lessonId = l2)
        createActivityInGroup(groupId, activityName = "a:"+l2)
        createStatementGoalInGroup(groupId, verbId = "v:"+l2, objectId = "o:"+l2)
        createAssignmentGoalInGroup(groupId, assignmentId = l2)
        createCourseGoalInGroup(groupId, courseId = l2)
        createTrainingEventInGroup(groupId, trainingEventId = l2)
        createWebContentGoalInGroup(groupId, webContentId = l2)

        val group2Id = createSubGoalGroup(groupId, s"g_${l1}_$l2")
        for {l3 <- 1 to 10} {
          createLessonGoalInGroup(group2Id, lessonId = l3)
          createActivityInGroup(group2Id, activityName = "a:"+l3)
          createStatementGoalInGroup(group2Id, verbId = "v:"+l3, objectId = "o:"+l3)
          createAssignmentGoalInGroup(group2Id, assignmentId = l3)
          createCourseGoalInGroup(groupId, courseId = l3)
          createTrainingEventInGroup(group2Id, trainingEventId = l3)
          createWebContentGoalInGroup(group2Id, webContentId = l3)
        }
      }
    }
    publish(lpId)

    val sourceTree = get(s"/learning-paths/$lpId/goals/tree")(body)

    post(s"/learning-paths/$lpId/draft") {
      status should beOk

      body should haveJson(
        s"""{
           |  "id":$lpId,
           |  "published":false,
           |  "title":"version 1 title",
           |  "openBadgesEnabled":false,
           |  "createdDate":"2017-03-06T15:30:52Z",
           |  "modifiedDate":"2017-03-06T15:30:52Z"
           |}""".stripMargin,
        ignoreValues = Seq("createdDate", "modifiedDate")
      )
    }

    get(s"/learning-paths/$lpId/draft/goals/tree") {
      status should beOk
      body should haveJson(
        sourceTree,
        ignoreValues = Seq("id", "groupId", "modifiedDate")
      )
    }
  }
}
