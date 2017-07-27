package com.arcusys.valamis.learningpath.web.servlets.members

import javax.servlet.http.HttpServletRequest

import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase
import com.arcusys.valamis.learningpath.web.servlets.base.PermissionChecker
import com.arcusys.valamis.learningpath.{LiferayHelperTestImpl, ServletImpl}
import com.arcusys.valamis.members.picker.model.MemberTypes
import com.arcusys.valamis.members.picker.model.impl.ForcedUserInfo
import com.arcusys.valamis.members.picker.service.LiferayHelper

/**
  * Created by mminin on 02/02/2017.
  */
class JoinLeaveTest extends LPServletTestBase {

  val testUserId = 101
  val otherUserId = 102
  val userAuthHeaders = Map(("userId", testUserId.toString))

  var hasModify = true

  override def servlet = new ServletImpl(dbInfo) {
    override def permissionChecker: PermissionChecker = new PermissionChecker(){
      override def hasPermission(permission: String)(implicit r: HttpServletRequest): Boolean = {
        hasModify
      }
    }
    override def liferayHelper: LiferayHelper = new LiferayHelperTestImpl(
      companyId,
      Seq(
        ForcedUserInfo(testUserId, "user 1", "/logo/u1", Nil, Nil, Nil, Nil),
        ForcedUserInfo(otherUserId, "user 2", "/logo/u2", Nil, Nil, Nil, Nil)
      )
    )
  }

  test("user is able to join to the published path") {
    hasModify = true

    val lpId = createLearningPath("path 1")
    publish(lpId)

    hasModify = false
    post(s"/learning-paths/$lpId/join",
      headers = userAuthHeaders
    ) {
      status should beNoContent
    }

    get(s"/learning-paths/$lpId/members/users") {
      status should beOk
      body should haveJson(
        s"""{
          |  "total": 1,
          |  "items": [{
          |    "id": $testUserId,
          |    "name":"user 1",
          |    "logo":"/logo/u1",
          |    "groups":[],
          |    "roles":[],
          |    "organizations":[],
          |    "membershipInfo":[{"id":$testUserId,"tpe":"user"}]
          |  }]
          |} """.stripMargin)
    }
  }

//  test("signed out user is not able to join to the path") {  todo: add checking and uncomment
//    hasModify = true
//
//    val lpId = createLearningPath("path 1")
//    publish(lpId)
//
//    hasModify = false
//    post(s"/learning-paths/$lpId/join") {
//      status should beNotAllowed
//    }
//  }
//
//  test("user without modify permission is not able to join to the unpublished path") {
//    hasModify = true
//    val lpId = createLearningPath("path 1")
//
//    hasModify = false
//    post(s"/learning-paths/$lpId/join",
//      userAuthHeaders
//    ) {
//      status should beNotAllowed
//    }
//  }
//
//  test("user without modify permission is not able to join to the inactive path") {
//    hasModify = true
//    val lpId = createLearningPath("path 1")
//    publish(lpId)
//    deactivate(lpId)
//
//    hasModify = false
//    post(s"/learning-paths/$lpId/join",
//      userAuthHeaders
//    ) {
//      status should beNotAllowed
//    }
//  }

  test("user is able to leave the published path") {
    hasModify = true
    val lpId = createLearningPath("path 1")

    publish(lpId)

    addMember(lpId, testUserId, MemberTypes.User)
    addMember(lpId, otherUserId, MemberTypes.User)

    hasModify = false
    post(s"/learning-paths/$lpId/leave",
      headers = userAuthHeaders
    ) {
      status should beNoContent
    }

    get(s"/learning-paths/$lpId/members/users") {
      status should beOk
      body should haveJson(
        s"""{
           |  "total": 1,
           |  "items": [{
           |    "id": $otherUserId,
           |    "name":"user 2",
           |    "logo":"/logo/u2",
           |    "groups":[],
           |    "roles":[],
           |    "organizations":[],
           |    "membershipInfo":[{"id":$otherUserId,"tpe":"user"}]
           |  }]
           |} """.stripMargin)
    }
  }

//  test("signed out user is not able to leave to the path") {   todo: add checking and uncomment
//    hasModify = true
//    val lpId = createLearningPath("path 1")
//    publish(lpId)
//
//    addMember(lpId, currentUserId, MemberTypes.User)
//    addMember(lpId, otherUserId, MemberTypes.User)
//
//    hasModify = false
//    post(s"/learning-paths/$lpId/leave") {
//      status should beNotAllowed
//    }
//  }
//
//  test("user without modify permission is not able to leave to the unpublished path") {
//    hasModify = true
//    val lpId = createLearningPath("path 1")
//
//    addMember(lpId, currentUserId, MemberTypes.User)
//    addMember(lpId, otherUserId, MemberTypes.User)
//
//    hasModify = false
//    post(s"/learning-paths/$lpId/leave",
//      userAuthHeaders
//    ) {
//      status should beNotAllowed
//    }
//  }
//
//  test("user without modify permission is not able to leave to the inactive path") {
//    hasModify = true
//    val lpId = createLearningPath("path 1")
//    publish(lpId)
//    deactivate(lpId)
//
//    addMember(lpId, currentUserId, MemberTypes.User)
//    addMember(lpId, otherUserId, MemberTypes.User)
//
//    hasModify = false
//    post(s"/learning-paths/$lpId/leave",
//      userAuthHeaders
//    ) {
//      status should beNotAllowed
//    }
//  }

}