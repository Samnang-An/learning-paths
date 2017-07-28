package com.arcusys.valamis.learningpath.web.servlets.permission.modify

import javax.servlet.http.HttpServletRequest

import com.arcusys.valamis.learningpath.ServletImpl
import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase
import com.arcusys.valamis.learningpath.web.servlets.base.PermissionChecker

class VisibilityTest extends LPServletTestBase {

  override lazy val servlet: ServletImpl = new ServletImpl(dbInfo) {
    override def permissionChecker = new PermissionChecker(){
      override def hasPermission(permission: String)
                                (implicit r: HttpServletRequest): Boolean = canModify
    }
  }

  var canModify = false


  test("not published LP should be hidden if no modify permission") {
    canModify = true

    val lp1Id = createLearningPath("test 1")
    val lp2Id = createLearningPath("test 2")
    val lp3Id = createLearningPath("test 3")

    publish(lp3Id)

    canModify = false

    get("/learning-paths") {
      status should beOk

      body should haveJson(
        s"""{
           | "total": 1,
           | "items":[{
           |   "id": $lp3Id,
           |   "activated": true,
           |   "published": true,
           |   "title": "test 3",
           |   "createdDate": "2017-01-31T11:23:18Z",
           |   "modifiedDate": "2017-01-31T11:23:18Z"
           |  }]
           |}""".stripMargin,
        ignoreValues = Seq("createdDate", "modifiedDate")
      )
    }
  }

  test("deactivate LP should be hidden if no modify permission") {
    canModify = true

    val lp1Id = createLearningPath("test 1")
    val lp2Id = createLearningPath("test 2")
    val lp3Id = createLearningPath("test 3")

    publish(lp1Id)
    publish(lp2Id)
    publish(lp3Id)

    deactivate(lp1Id)
    deactivate(lp3Id)

    canModify = false

    get("/learning-paths") {
      status should beOk

      body should haveJson(
        s"""{
           | "total": 1,
           | "items":[{
           |   "id": $lp2Id,
           |   "activated": true,
           |   "published": true,
           |   "title": "test 2",
           |   "createdDate": "2017-01-31T11:23:18Z",
           |   "modifiedDate": "2017-01-31T11:23:18Z"
           |  }]
           |}""".stripMargin,
        ignoreValues = Seq("createdDate", "modifiedDate")
      )
    }
  }

  test("return only activated paths for current user without modify permission") {
    canModify = true
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

    canModify = false
    get("/users/current/learning-paths?sort=-title") {
      status should beOk

      body should haveJson(
        s"""{
           |  "total": 2,
           |  "items": [{
           |      "id": $publishedLpId,
           |      "published":true,
           |      "title":"published",
           |      "userMembersCount": 0,
           |      "goalsCount": 0,
           |      "hasDraft": false
           |  }, {
           |      "id": $activeLpId,
           |      "published":true,
           |      "title":"active",
           |      "userMembersCount": 0,
           |      "goalsCount": 0,
           |      "hasDraft": false
           |  }]
           |}""".stripMargin)
    }
  }

  test("do not return data about unpublished path for user without modify permission") {
    canModify = true
    val unpublishedLpId = createLearningPath("unpublished")

    canModify = false
    get(s"/users/current/learning-paths/$unpublishedLpId") {
      status should beForbidden
    }
  }

  test("do not return data about inactive path for user without modify permission") {
    canModify = true
    val inactiveLpId = createLearningPath("inactive")
    publish(inactiveLpId)
    deactivate(inactiveLpId)

    canModify = false
    get(s"/users/current/learning-paths/$inactiveLpId") {
      status should beForbidden
    }
  }
}
