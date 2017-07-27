package com.arcusys.valamis.learningpath.web.servlets.patternreport

import com.arcusys.valamis.learningpath.listeners.LRActivityListener
import com.arcusys.valamis.learningpath.models.LRActivityType
import com.arcusys.valamis.learningpath.{LiferayHelperTestImpl, ServletImpl}
import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase
import com.arcusys.valamis.learningpath.web.servlets.utils.{LRActivity, LRActivityTypeServiceImpl}
import com.arcusys.valamis.members.picker.model.impl.ForcedUserInfo
import com.arcusys.valamis.members.picker.model.{IdAndName, MemberTypes}
import com.arcusys.valamis.members.picker.service.LiferayHelper

class LearningPatternReportTest extends LPServletTestBase {

  val user1Id = 101
  val user2Id = 102
  val companyId = 203
  val socialActivityType = "blogs"

  override lazy val servlet = new ServletImpl(dbInfo) {
    override def liferayHelper: LiferayHelper = new LiferayHelperTestImpl(
      companyId,
      users = Seq(
        ForcedUserInfo(user1Id, "user 1", "/logo/u1", Nil, Nil, Nil, Nil),
        ForcedUserInfo(user2Id, "user 2", "/logo/u2", Nil, Nil, Nil, Nil)
      ))

    override val lrActivityTypeService = new LRActivityTypeServiceImpl(
      Seq(
        LRActivityType("activityType1", "Test activity1"),
        LRActivityType("activityType2", "Test activity2")
      )
    )
  }

  test("get certificates /course/all/certificate") {
    val lp1Id = createLearningPath("path 1", courseId = Some(1))

    val lessonId = createLessonGoal(lp1Id, 1)
    val activityId = createActivityGoal(lp1Id, "activityType1", Some(3))
    val assignmentId = createAssignmentGoal(lp1Id, 1)
    val webContentId = createWebContentGoal(lp1Id, 1)
    val trainingEventId = createTrainingEventGoal(lp1Id, 1)
    val statementId = createStatementGoal(lp1Id)
    val courseGoalId = createCourseGoal(lp1Id, 1)
    publish(lp1Id)

    val lp2Id = createLearningPath("path 2", courseId = Some(2))
    publish(lp2Id)

    get(s"/learning-pattern-report/course/all/certificate") {
      status should beOk
      body should haveJson(
        s"""[
           |  {
           |    "title": "path 1",
           |    "id": 1,
           |    "creationDate": "2017-03-29T09:57:02Z",
           |    "goals": [
           |      {
           |        "id": $activityId,
           |        "goalType": 1,
           |        "isOptional": false,
           |        "title": "3 Test activity1",
           |        "activityName": "activityType1"
           |      },
           |      {
           |        "id": $lessonId,
           |        "goalType": 4,
           |        "isOptional": false,
           |        "title": "Deleted lesson with id 1",
           |        "lessonId": 1
           |      },
           |      {
           |        "id": $assignmentId,
           |        "goalType": 5,
           |        "isOptional": false,
           |        "title": "Deleted assignment with id 1",
           |        "assignmentId": 1
           |      },
           |      {
           |        "id": $courseGoalId,
           |        "goalType": 2,
           |        "isOptional": false,
           |        "title": "Deleted course with id 1",
           |        "courseId": 1
           |      },
           |      {
           |        "id": $trainingEventId,
           |        "goalType": 6,
           |        "isOptional": false,
           |        "title": "event",
           |        "eventId": 1,
           |        "startTime": "2017-03-29T09:57:04Z",
           |        "endTime": "2017-03-29T09:57:04Z"
           |      },
           |      {
           |        "id": $statementId,
           |        "goalType": 3,
           |        "isOptional": false,
           |        "title": "http://adlnet.gov/expapi/verbs/experienced http://example.com/website",
           |        "obj": "http://example.com/website",
           |        "verb": "http://adlnet.gov/expapi/verbs/experienced"
           |      },
           |      {
           |        "id": $webContentId,
           |        "goalType": 7,
           |        "isOptional": false,
           |        "title": "Deleted webContent with id 1"
           |      }
           |    ]
           |  },
           |  {
           |    "title": "path 2",
           |    "id": 2,
           |    "creationDate": "2017-03-29T09:57:04Z",
           |    "goals": [
           |    ]
           |  }
           |]""".stripMargin,
        ignoreValues = Seq("creationDate", "startTime", "endTime")
      )
    }
  }

  test("get users with certificate statuses /course/all/users") {
    val lp1Id = createLearningPath("path 1", courseId = Some(1))
    createActivityGoal(lp1Id, socialActivityType, count = Some(1))
    addMember(lp1Id, user1Id, MemberTypes.User)
    publish(lp1Id)

    servlet.lrActivityTypeService.addActivity(LRActivity(user1Id, socialActivityType))
    await {
      new LRActivityListener(servlet.dbActions, servlet.taskManager)(servlet.executionContext)
        .onLRActivityCreated(user1Id, socialActivityType)(companyId)
    }

    val lp2Id = createLearningPath("path 2", courseId = Some(2))
    createActivityGoal(lp2Id)
    addMember(lp2Id, user1Id, MemberTypes.User)
    publish(lp2Id)

    get(s"/learning-pattern-report/course/all/users") {
      status should beOk
      body should haveJson(
        """[
          |  {
          |    "id": 101,
          |    "user": {
          |      "id": 101,
          |      "name": "user 1",
          |      "picture": "/logo/u1"
          |    },
          |    "organizations": [],
          |    "certificates": [
          |      {
          |        "certificateId": 1,
          |        "userId": 101,
          |        "status": 1
          |      },
          |      {
          |        "certificateId": 2,
          |        "userId": 101,
          |        "status": 3
          |      }
          |    ]
          |  }
          |]""".stripMargin)
    }
  }

  test("get summary statuses /course/all/total") {
    val lp1Id = createLearningPath("path 1", courseId = Some(1))
    createActivityGoal(lp1Id, socialActivityType, count = Some(1))
    addMember(lp1Id, user1Id, MemberTypes.User)
    addMember(lp1Id, user2Id, MemberTypes.User)
    publish(lp1Id)

    servlet.lrActivityTypeService.addActivity(LRActivity(user1Id, socialActivityType))
    await {
      new LRActivityListener(servlet.dbActions, servlet.taskManager)(servlet.executionContext)
        .onLRActivityCreated(user1Id, socialActivityType)(companyId)
    }

    val lp2Id = createLearningPath("path 2", courseId = Some(2))
    createAssignmentGoal(lp2Id, 1)
    addMember(lp2Id, user1Id, MemberTypes.User)
    publish(lp2Id)

    get(s"/learning-pattern-report/course/all/total") {
      status should beOk
      body should haveJson(
        """[
          |  {
          |    "id": 2,
          |    "total": {
          |      "3": 1
          |    }
          |  },
          |  {
          |    "id": 1,
          |    "total": {
          |      "1": 1,
          |      "3": 1
          |    }
          |  }
          |]""".stripMargin
      )
    }
  }

  test("get certificates /course/all/usersCount") {
    val lp1Id = createLearningPath("path 1", courseId = Some(1))
    addMember(lp1Id, user1Id, MemberTypes.User)
    addMember(lp1Id, user2Id, MemberTypes.User)
    publish(lp1Id)

    val lp2Id = createLearningPath("path 2", courseId = Some(2))
    publish(lp2Id)

    get(s"/learning-pattern-report/course/all/usersCount") {
      status should beOk
      body should haveJson(
        s"""{
           |  "result": 2
           |}""".stripMargin
      )
    }
  }

  test("get certificate goals /course/:id/certificate/:id/goals") {
    val courseId = 1
    val lp1Id = createLearningPath("path 1", courseId = Some(courseId))
    createActivityGoal(lp1Id, socialActivityType, count = Some(1))
    createActivityGoal(lp1Id, "comments", count = Some(1))
    addMember(lp1Id, user1Id, MemberTypes.User)
    addMember(lp1Id, user2Id, MemberTypes.User)
    publish(lp1Id)

    servlet.lrActivityTypeService.addActivity(LRActivity(user1Id, socialActivityType))
    await {
      new LRActivityListener(servlet.dbActions, servlet.taskManager)(servlet.executionContext)
        .onLRActivityCreated(user1Id, socialActivityType)(companyId)
    }

    get(s"/learning-pattern-report/course/$courseId/certificate/$lp1Id/goals?userIds[]=100&&userIds[]=$user1Id&userIds[]=$user2Id") {
      status should beOk
      body should haveJson(
        """[
          |  {
          |    "certificateId": 1,
          |    "userId": 101,
          |    "goals": [
          |      {
          |        "goalId": 1,
          |        "date": "2017-03-29T10:15:33Z",
          |        "status": 1
          |      },
          |      {
          |        "goalId": 2,
          |        "date": "2017-03-29T10:15:33Z",
          |        "status": 3
          |      }
          |    ]
          |  },
          |  {
          |    "certificateId": 1,
          |    "userId": 102,
          |    "goals": [
          |      {
          |        "goalId": 1,
          |        "date": "2017-03-29T10:15:33Z",
          |        "status": 3
          |      },
          |      {
          |        "goalId": 2,
          |        "date": "2017-03-29T10:15:33Z",
          |        "status": 3
          |      }
          |    ]
          |  }
          |]""".stripMargin,
        ignoreValues = Seq("date")
      )
    }
  }


  test("get total goal statuses /course/all/certificate/1/total") {
    val courseId = 1
    val lp1Id = createLearningPath("path 1", courseId = Some(courseId))
    val goal1Id = createActivityGoal(lp1Id, socialActivityType, count = Some(1))
    val goal2Id = createActivityGoal(lp1Id, "comments", count = Some(1))
    addMember(lp1Id, user1Id, MemberTypes.User)
    addMember(lp1Id, user2Id, MemberTypes.User)
    publish(lp1Id)

    servlet.lrActivityTypeService.addActivity(LRActivity(user1Id, socialActivityType))
    await {
      new LRActivityListener(servlet.dbActions, servlet.taskManager)(servlet.executionContext)
        .onLRActivityCreated(user1Id, socialActivityType)(companyId)
    }

    get(s"/learning-pattern-report/course/all/certificate/$lp1Id/total") {
      status should beOk

      body should haveJson(
        s"""[
           |  {
           |    "id": $goal1Id,
           |    "total": {
           |      "1": 1,
           |      "3": 1
           |    }
           |  },
           |  {
           |    "id": $goal2Id,
           |    "total": {
           |      "3": 2
           |    }
           |  }
           |]""".stripMargin
      )
    }
  }
}
