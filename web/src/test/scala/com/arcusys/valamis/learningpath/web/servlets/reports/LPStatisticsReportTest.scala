package com.arcusys.valamis.learningpath.web.servlets.reports

import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase
import com.arcusys.valamis.learningpath.{LiferayHelperTestImpl, ServletImpl}
import com.arcusys.valamis.members.picker.model.MemberTypes
import com.arcusys.valamis.members.picker.model.impl.ForcedUserInfo
import com.arcusys.valamis.members.picker.service.LiferayHelper

/**
  * Created by mminin on 04/04/2017.
  */
class LPStatisticsReportTest extends {
  val testUserId = 101
  val user2Id = 102
  val user3Id = 103

  implicit val testCompanyId = -1L
} with LPServletTestBase {

  override lazy val servlet = new ServletImpl(dbInfo) {
    override def liferayHelper: LiferayHelper = new LiferayHelperTestImpl(
      testCompanyId,
      users = Seq(
        ForcedUserInfo(testUserId, "user 1", "/logo/u1", Nil, Nil, Nil, Nil),
        ForcedUserInfo(user2Id, "user 2", "/logo/u2", Nil, Nil, Nil, Nil),
        ForcedUserInfo(user3Id, "user 3", "/logo/u2", Nil, Nil, Nil, Nil)
      ))
  }
  val userAuthHeaders = Map(("userId", testUserId.toString))

  test("get from empty learning path") {
    val lpId = createLearningPath("path 1")
    publish(lpId)

    get(s"/learning-statistics-report/learning-paths/$lpId/users") {
      status should beOk
      body should haveJson(""" {"total":0,"items":[] } """)
    }

    get(s"/learning-statistics-report/learning-paths/") {
      status should beOk

      body should haveJson(
        """{
          |  "total": 1,
          |  "items": [ {"id":1,"title":"path 1","statusToCount":{}} ]
          |} """.stripMargin
      )
    }
  }

  test("get from published learning path") {
    val lpId = createLearningPath("path 1")

    addMember(lpId, testUserId, MemberTypes.User)
    addMember(lpId, user2Id, MemberTypes.User)

    createActivityGoal(lpId, "test activity")

    val groupId = createGoalGroup(lpId, "test group")
    createActivityInGroup(groupId, "test activity")
    createActivityInGroup(groupId, "test activity 2")

    publish(lpId)

    get(s"/learning-statistics-report/learning-paths/$lpId/users") {
      status should beOk

      body should haveJson(
        s"""{
           |  "total":2,
           |  "items":[
           |    { "id": $testUserId, "statusToCount": { "InProgress":3 }},
           |    { "id": $user2Id, "statusToCount": { "InProgress":3 }}
           |  ]
           |} """.stripMargin)
    }

    get(s"/learning-statistics-report/learning-paths/") {
      status should beOk

      body should haveJson(
        """{
          |  "total": 1,
          |  "items": [ {"id":1,"title":"path 1","statusToCount": {"InProgress":2} } ]
          |} """.stripMargin
      )
    }
  }

  test("get from learning path with completed goals") {
    val lpId = createLearningPath("path 1")

    addMember(lpId, testUserId, MemberTypes.User)
    addMember(lpId, user2Id, MemberTypes.User)

    createActivityGoal(lpId, "test activity")

    val groupId = createGoalGroup(lpId, "test group")
    createActivityInGroup(groupId, "test activity")
    createActivityInGroup(groupId, "test activity 2")

    publish(lpId)

    userCreatesNewLRActivity(servlet, testUserId, "test activity")

    get(s"/learning-statistics-report/learning-paths/$lpId/users") {
      status should beOk

      body should haveJson(
        s"""{
           |  "total":2,
           |  "items":[
           |    { "id": $testUserId, "statusToCount": { "Success":2, "InProgress":1 }},
           |    { "id": $user2Id, "statusToCount": { "InProgress":3 }}
           |  ]
           |} """.stripMargin)
    }

    get(s"/learning-statistics-report/learning-paths/") {
      status should beOk

      body should haveJson(
        """{
          |  "total": 1,
          |  "items": [ {"id":1,"title":"path 1","statusToCount": {"InProgress":2} } ]
          |} """.stripMargin
      )
    }
  }

  test("get from completed learning path") {
    val lpId = createLearningPath("path 1")
    val lp2Id = createLearningPath("path 2")

    addMember(lpId, testUserId, MemberTypes.User)
    addMember(lpId, user2Id, MemberTypes.User)

    createActivityGoal(lpId, "test activity")

    val groupId = createGoalGroup(lpId, "test group")
    createActivityInGroup(groupId, "test activity")

    publish(lpId)
    publish(lp2Id)

    userCreatesNewLRActivity(servlet, testUserId, "test activity")

    get(s"/learning-statistics-report/learning-paths/$lpId/users") {
      status should beOk

      body should haveJson(
        s"""{
           |  "total":2,
           |  "items":[
           |    { "id": $testUserId, "statusToCount": { "Success":2 }},
           |    { "id": $user2Id, "statusToCount": { "InProgress":2 }}
           |  ]
           |} """.stripMargin)
    }

    get(s"/learning-statistics-report/learning-paths/") {
      status should beOk

      body should haveJson(
        """{
          |  "total": 2,
          |  "items": [
          |    {"id":1,"title":"path 1","statusToCount": {"InProgress":1, "Success":1} },
          |    {"id":2,"title":"path 2","statusToCount": { } }
          |  ]
          |} """.stripMargin
      )
    }
  }

  test("should return only published and activated paths") {
    val unpublishedLpId = createLearningPath("unpublished")
    val publishedLpId = createLearningPath("published")
    val inactiveLpId = createLearningPath("inactive")
    val activeLpId = createLearningPath("active")

    publish(publishedLpId)

    publish(inactiveLpId)
    deactivate(inactiveLpId)

    publish(activeLpId)
    deactivate(activeLpId)
    activate(activeLpId)

    get(s"/learning-statistics-report/learning-paths/") {
      status should beOk

      body should haveJson(
        s"""{
           |  "total": 2,
           |  "items": [
           |    {"id":$activeLpId,"title":"active","statusToCount":{}},
           |    {"id":$publishedLpId,"title":"published","statusToCount":{}}
           |  ]
           |} """.stripMargin
      )
    }
  }
}
