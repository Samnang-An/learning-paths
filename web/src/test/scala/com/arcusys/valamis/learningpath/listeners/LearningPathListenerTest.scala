package com.arcusys.valamis.learningpath.listeners

import com.arcusys.valamis.learningpath.listeners.model.{LPInfo, LPInfoWithUserStatus}
import com.arcusys.valamis.learningpath.models._
import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase
import com.arcusys.valamis.learningpath.web.servlets.utils.LRActivity
import com.arcusys.valamis.learningpath.{LiferayHelperTestImpl, ServletImpl}
import com.arcusys.valamis.members.picker.model.MemberTypes
import com.arcusys.valamis.members.picker.model.impl.ForcedUserInfo
import com.arcusys.valamis.members.picker.service.LiferayHelper
import org.joda.time.{DateTime, Period, PeriodType}


class LearningPathListenerTest extends LPServletTestBase {

  val socialActivityType = "blogs"
  val testCompanyId = 1024L
  val lp1Title = "path 1"

  val companyHeaders = Map("companyId" -> testCompanyId.toString)

  override def servlet: ServletImpl = new ServletImpl(dbInfo) {
    override def liferayHelper: LiferayHelper = new LiferayHelperTestImpl(
      testCompanyId,
      users = Seq(
        ForcedUserInfo(user1Id, "user 1", "/logo/u1", Nil, Nil, Nil, Nil),
        ForcedUserInfo(user2Id, "user 2", "/logo/u1", Nil, Nil, Nil, Nil)
      ))
  }

  val learningPathListener: LearningPathListener =
    new LearningPathListener(servlet.dbActions, servlet.learningPathService, servlet.taskManager,
      "delegate/learning-paths/logo-files")(servlet.executionContext)


  private val user1Id = 101L
  private val user2Id = 102L

  test("get learning path by id") {
    val lp1Id = createSucceedLearningPath()

    val lp = await {
      learningPathListener.getLPById(lp1Id, testCompanyId)
    }.get

    lp.version.title should equal(lp1Title)
  }

  test("get users succeed certificates count for valamis report") {
    createSucceedLearningPath()
    val now = DateTime.now()
    val startDate = now.minusWeeks(1)
    val endDate = now.plusWeeks(1)

    val summary = await {
      learningPathListener.getUsersToLPCount(startDate, endDate, testCompanyId)
    }

    summary(user1Id) should equal(1)
  }

  test("get lp by ids") {
    createLearningPaths()
    val logo2Url = setLogoToDraft(2, Array[Byte](1, 2, 3), "png", companyHeaders)
    val logo3Url = setLogoToDraft(3, Array[Byte](1, 2, 3), "png", companyHeaders)
    publish(2, companyHeaders)

    val lps = await {
      learningPathListener.getLearningPathsByIds(Seq(2L, 3L))
    }

    lps shouldBe Seq(
      LPInfo(2, 1024, activated = true, "LP2", Some("LP2 desc"),
        Some(s"delegate/learning-paths/$logo2Url")),
      LPInfo(3, 1024, activated = false, "LP3", Some("LP3 desc"),
        Some(s"delegate/learning-paths/$logo3Url"))
    )
  }

  test("get lp with status by ids") {
    createLearningPaths()
    val logo2Url = setLogoToDraft(2, Array[Byte](1, 2, 3), "png", companyHeaders)
    val logo3Url = setLogoToDraft(3, Array[Byte](1, 2, 3), "png", companyHeaders)

    addMember(2, user1Id, MemberTypes.User, companyHeaders)
    addMember(3, user1Id, MemberTypes.User, companyHeaders)

    createActivityGoal(2,"test", None, headers = companyHeaders)

    publish(2, companyHeaders)
    publish(3, companyHeaders)


    val lps = await {
      learningPathListener.getLearningPathWithStatusByIds(user1Id, Seq(1L, 2L, 3L))
    }

    lps shouldBe Seq(
      LPInfoWithUserStatus(1, 1024, activated = false, "LP1", Some(Period.months(5)), Some("LP1 desc"),
        None, None, None, None),
      LPInfoWithUserStatus(2, 1024, activated = true, "LP2", Some(Period.months(5)), Some("LP2 desc"),
        Some(s"delegate/learning-paths/$logo2Url"), Some(CertificateStatuses.InProgress), lps(1).statusDate, Some(0.0D)),
      LPInfoWithUserStatus(3, 1024, activated = true, "LP3", Some(Period.years(3)), Some("LP3 desc"),
        Some(s"delegate/learning-paths/$logo3Url"),
        Some(CertificateStatuses.InProgress), lps(2).statusDate, Some(0.0D))
    )
  }

  test("get passed lp") {
    createLearningPaths()
    val logo3Url = setLogoToDraft(3, Array[Byte](1, 2, 3), "png", companyHeaders)

    addMember(2, user1Id, MemberTypes.User, companyHeaders)
    addMember(3, user1Id, MemberTypes.User, companyHeaders)

    createActivityGoal(3,"test", None, optional = false, headers = companyHeaders)

    publish(2, companyHeaders)
    publish(3, companyHeaders)

    userCreatesNewLRActivity(servlet, user1Id, "test")(testCompanyId)

    val lps = await {
      learningPathListener.getPassedLearningPath(user1Id, testCompanyId)
    }

    lps shouldBe Seq(
      LPInfoWithUserStatus(3, 1024, activated = true, "LP3", Some(Period.years(3)), Some("LP3 desc"),
        Some(s"delegate/learning-paths/$logo3Url"),
        Some(CertificateStatuses.Success), lps(0).statusDate, Some(1.0D)
      ))

  }

  test("get lp by id") {
    createLearningPaths()
    val logo2Url = setLogoToDraft(2, Array[Byte](1, 2, 3), "png", companyHeaders)
    publish(2, companyHeaders)

    val lp = await {
      learningPathListener.getLPById(2, testCompanyId)
    }

    val now = DateTime.now
    lp map { lp =>
      lp.copy(version = lp.version.copy(createdDate = now, modifiedDate = now))
    } shouldBe Some(LearningPathWithVersion(LearningPath(2, activated = true, 1024, -1, hasDraft = false, Some(2L)),
      LPVersion(2, "LP2", Some("LP2 desc"),
        Some(logo2Url.split("/").last), None,
        Some(Period.months(5)), Some(Period.days(10)),
        openBadgesEnabled = true,
        Some("obd"), published = true,
        now,
        now)))

  }

  private def createLearningPaths(): Unit = {
    post("/learning-paths",
      s"""{
         |  "title": "LP1",
         |  "description": "LP1 desc",
         |  "courseId": 23,
         |  "validPeriod": "P5M",
         |  "expiringPeriod": "P10D",
         |  "openBadgesEnabled": true,
         |  "openBadgesDescription": "obd"
         |} """.stripMargin,
      jsonContentHeaders ++ companyHeaders
    )(status should beOk)
    post("/learning-paths",
      s"""{
         |  "title": "LP2",
         |  "description": "LP2 desc",
         |  "validPeriod": "P5M",
         |  "expiringPeriod": "P10D",
         |  "openBadgesEnabled": true,
         |  "openBadgesDescription": "obd"
         |} """.stripMargin,
      jsonContentHeaders ++ companyHeaders
    )(status should beOk)
    post("/learning-paths",
      s"""{
         |  "title": "LP3",
         |  "description": "LP3 desc",
         |  "courseId": 23,
         |  "validPeriod": "P3Y",
         |  "expiringPeriod": "P10D",
         |  "openBadgesEnabled": false
         |} """.stripMargin,
      jsonContentHeaders ++ companyHeaders
    )(status should beOk)
    post("/learning-paths",
      s"""{
         |  "title": "LP4",
         |  "description": "LP4 desc",
         |  "courseId": 23,
         |  "validPeriod": "P5M",
         |  "expiringPeriod": "P10D",
         |  "openBadgesEnabled": true,
         |  "openBadgesDescription": "obd"
         |} """.stripMargin,
      jsonContentHeaders ++ companyHeaders
    )(status should beOk)
  }

  protected def createSucceedLearningPath(): Long = {
    val lp1Id = createLearningPath(lp1Title, courseId = Some(1), companyHeaders)
    createActivityGoal(lp1Id, socialActivityType, count = Some(1), headers = companyHeaders)
    addMember(lp1Id, user1Id, MemberTypes.User, companyHeaders)
    addMember(lp1Id, user2Id, MemberTypes.User, companyHeaders)
    publish(lp1Id, companyHeaders)

    userCreatesNewLRActivity(servlet, user1Id, socialActivityType)(testCompanyId)

    lp1Id
  }
}