package com.arcusys.valamis.learningpath.web.servlets.learningpath

import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase
import com.arcusys.valamis.learningpath.{LiferayHelperTestImpl, ServletImpl}
import com.arcusys.valamis.members.picker.model.{IdAndName, MemberTypes}
import com.arcusys.valamis.members.picker.model.impl.ForcedUserInfo
import com.arcusys.valamis.members.picker.service.LiferayHelper
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.util.Random

/**
  * Created by mminin on 10/04/2017.
  */
class CloneTest extends {
  val testCompanyId = 184
  val testUserId = 287
  val authHeaders = Map("companyId" -> testCompanyId.toString, "userId" -> testUserId.toString)

  val testUserGroup = IdAndName(3, "g_3")
  val testRole = IdAndName(5, "r_5")
  val testOrganization = IdAndName(6, "o_6")

} with LPServletTestBase {

  override lazy val servlet = new ServletImpl(dbInfo) {
    override lazy val liferayHelper: LiferayHelper = new LiferayHelperTestImpl(
      testCompanyId,
      users = Seq(ForcedUserInfo(testUserId, "user 1", "/logo/u1", Nil, Nil, Nil, Nil) ),
      roles = Seq(testRole),
      organizations = Seq(testOrganization),
      userGroups = Seq(testUserGroup)
    )
  }


  test("with wrong id should return 404") {
    val fakeId = 2345
    post(s"/learning-paths/$fakeId/clone") {
      status should beNotFound
      body should be("""{"message":"no learning path with id: 2345"}""")
    }
  }

  test("should return created model") {
    val courseId = 123
    val lpId = createLearningPath("source LP", courseId = Some(courseId), authHeaders)

    post(s"/learning-paths/$lpId/clone", headers = authHeaders) {
      status should beOk
      body should haveJson(
        s"""{
           |  "id":2,
           |  "activated":false,
           |  "hasDraft":true,
           |  "published":false,
           |  "title":"source LP",
           |  "courseId":$courseId,
           |  "openBadgesEnabled":false,
           |  "createdDate":"2017-04-10T11:53:30Z",
           |  "modifiedDate":"2017-04-10T11:53:30Z"
           |}""".stripMargin,
        ignoreValues = Seq("id", "createdDate", "modifiedDate")
      )
    }
  }

  test("should not copy members") {
    val courseId = 123
    val lpId = createLearningPath("source LP", courseId = Some(courseId), authHeaders)

    addMember(lpId, testUserId, MemberTypes.User)
    addMember(lpId, testUserGroup.id, MemberTypes.UserGroup)
    addMember(lpId, testRole.id, MemberTypes.Role)
    addMember(lpId, testOrganization.id, MemberTypes.Organization)

    val newLpId = cloneLearningPath(lpId, authHeaders)

    get(s"/learning-paths/$newLpId/members/users") {
      status should beOk
      body should be("""{"items":[],"total":0}""")
    }
    get(s"/learning-paths/$newLpId/members/groups") {
      status should beOk
      body should be("""{"items":[],"total":0}""")
    }
    get(s"/learning-paths/$newLpId/members/roles") {
      status should beOk
      body should be("""{"items":[],"total":0}""")
    }
    get(s"/learning-paths/$newLpId/members/organizations") {
      status should beOk
      body should be("""{"items":[],"total":0}""")
    }
  }

  test("should clone goals from current version only") {
    val lpId = createLearningPath("source v 0")

    //create version 1
    createGoalGroup(lpId, "group 1")
    publish(lpId)

    //create version 2, it will be current
    createNewDraft(lpId)
    createGoalsTreeWithAllTypes(lpId)
    publish(lpId)

    //create draft version
    createNewDraft(lpId)
    createGoalsTreeWithAllTypes(lpId)

    //clone
    val newLpId = post(s"/learning-paths/$lpId/clone") {
      status should beOk
      (parse(body) \ "id").extract[Long]
    }

    //check goals
    val origionaGoalsTree = get(s"/learning-paths/$lpId/goals/tree") {
      status should beOk
      body
    }

    val newGoalsTree = get(s"/learning-paths/$newLpId/goals/tree") {
      status should beOk
      body
    }

    newGoalsTree should haveJson(
      origionaGoalsTree,
      ignoreValues = Seq("id", "modifiedDate", "groupId")
    )
  }


  test("should clone logo") {
    val logo = new Array[Byte](100)
    Random.nextBytes(logo)

    val lpId = createLearningPath("source LP", headers = authHeaders)

    setLogoToDraft(lpId, logo, headers = authHeaders)

    val newLpId = cloneLearningPath(lpId, authHeaders)

    deleteLearningPath(lpId, authHeaders)

    val logoUrl = get(s"/learning-paths/$newLpId", headers = authHeaders) {
      status should beOk
      (parse(body) \ "logoUrl").extract[String]
    }

    get("/" + logoUrl, headers = authHeaders) {
      status should beOk

      bodyBytes should be(logo)
    }
  }
}
