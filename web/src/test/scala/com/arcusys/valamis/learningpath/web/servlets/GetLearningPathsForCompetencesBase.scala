package com.arcusys.valamis.learningpath.web.servlets

import com.arcusys.valamis.learningpath.services.GroupService
import com.arcusys.valamis.learningpath.web.impl.GroupServiceTestImpl
import com.arcusys.valamis.learningpath.{LiferayHelperTestImpl, ServletImpl}
import com.arcusys.valamis.members.picker.model.{IdAndName, MemberTypes}
import com.arcusys.valamis.members.picker.model.impl.ForcedUserInfo
import com.arcusys.valamis.members.picker.service.LiferayHelper
import org.json4s.jackson.JsonMethods.parse

/**
  * Created by pkornilov on 5/31/17.
  */
class GetLearningPathsForCompetencesBase extends LPServletTestBase {

  val user1Id = 101L
  val user2Id = 102L
  val user3Id = 103L

  val userGroup1 = IdAndName(201L, "User group 1")
  val userGroup2 = IdAndName(202L, "User group 2")

  val role1 = IdAndName(301L, "Role 1")
  val role2 = IdAndName(302L, "Role 2")

  val organization1 = IdAndName(401L, "Organization 1")
  val organization2 = IdAndName(402L, "Organization 2")

  implicit val defaultCompanyId: Long = 987
  val companyHeaders = Map("companyId" -> defaultCompanyId.toString)

  override def servlet = new ServletImpl(dbInfo) {
    override def companyId: Long = defaultCompanyId

    override lazy val liferayHelper: LiferayHelper = new LiferayHelperTestImpl(
      defaultCompanyId,
      users = Seq(
        ForcedUserInfo(user1Id, "user 1", "/logo1",
          groups = Seq(userGroup1),
          roles = Seq(role2),
          organizations = Seq(organization1), Nil),

        ForcedUserInfo(user2Id, "user 2", "/logo2",
          groups = Seq(userGroup1),
          roles = Seq(role1),
          organizations = Seq(organization2), Nil),

        ForcedUserInfo(user3Id, "user 3", "/logo1",
          groups = Seq(userGroup2),
          roles = Seq(role1),
          organizations = Seq(organization1), Nil)),

      roles = Seq(role1, role2),
      userGroups = Seq(userGroup1, userGroup2),
      organizations = Seq(organization1, organization2)
    )

    override lazy val groupService: GroupService = new GroupServiceTestImpl(
      groupIds = Map(
        MemberTypes.UserGroup -> liferayHelper.getUserGroups(defaultCompanyId).map(_.id),
        MemberTypes.Role -> liferayHelper.getRoles(defaultCompanyId).map(_.id),
        MemberTypes.Organization -> liferayHelper.getOrganizations(defaultCompanyId).map(_.id)
      )
    )
  }

  protected def createTestData(): String = {
    val lp1Id = createLearningPath("lp1", headers = companyHeaders)
    createGoalAndUsers(lp1Id, user1Id, user2Id, user3Id)
    makeLpSuccessForUser(lp1Id, user2Id, user3Id) //make success not for all users to
    //check that only succeeded users are included

    val lp2Id = post("/learning-paths",
      s"""{
         |  "title": "lp2",
         |  "description": "lp2 desc",
         |  "courseId": 23,
         |  "validPeriod": "P5M",
         |  "expiringPeriod": "P10D",
         |  "openBadgesEnabled": true,
         |  "openBadgesDescription": "obd"
         |} """.stripMargin,
      jsonContentHeaders ++ companyHeaders
    ) {
      status should beOk
      (parse(body) \ "id").extract[Long]
    }
    val lp2LogoUrl = setLogoToDraft(lp2Id, Array[Byte](1, 2, 3), headers = companyHeaders)

    createGoalAndUsers(lp2Id, user1Id, user3Id)
    makeLpSuccessForUser(lp2Id, user1Id, user3Id)


    val lp3Id = createLearningPath("other lp1", headers = companyHeaders)
    createGoalAndUsers(lp3Id, user1Id, user2Id)
    makeLpSuccessForUser(lp3Id, user2Id)

    //to check that deactivated LPs aren't included
    val lp4Id = createLearningPath("other lp2", headers = companyHeaders)
    createGoalAndUsers(lp4Id, user3Id)
    makeLpSuccessForUser(lp4Id, user3Id)
    deactivate(lp4Id)

    //to check that only LPs with succeeded users are included
    val lp5Id = createLearningPath("other lp3", headers = companyHeaders)
    createGoalAndUsers(lp5Id, user1Id, user2Id)

    lp2LogoUrl
  }

  protected def createGoalAndUsers(lpId: Long, userIds: Long*): Long = {
    createActivityGoal(lpId, s"lp $lpId activity", headers = companyHeaders)
    addMembers(lpId, userIds, MemberTypes.User, headers = companyHeaders)
    publish(lpId, headers = companyHeaders)
    lpId
  }

  protected def makeLpSuccessForUser(lpId: Long, userIds: Long*): Unit = {
    userIds foreach { userId =>
      userCreatesNewLRActivity(servlet, userId, s"lp $lpId activity")(defaultCompanyId)
    }

  }


}
