package com.arcusys.valamis.learningpath.web.servlets.goals.checkers

import com.arcusys.valamis.learningpath.listeners.StatementListener
import com.arcusys.valamis.learningpath.models.StatementInfo
import com.arcusys.valamis.learningpath.{LiferayHelperTestImpl, ServletImpl}
import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase
import com.arcusys.valamis.members.picker.model.MemberTypes
import com.arcusys.valamis.members.picker.model.impl.ForcedUserInfo
import com.arcusys.valamis.members.picker.service.LiferayHelper
import org.joda.time.{DateTime, Period}

/**
  * Created by mminin on 20/03/2017.
  */
class StatementGoalCheckerTest extends LPServletTestBase {

  val verbId = "http://verbs.org/complete"
  val objectId = "http://example.org/test_1"
  val user1Id = 324
  val defaultCompanyId = 213

  override lazy val servlet: ServletImpl = new ServletImpl(dbInfo) {
    override def companyId = defaultCompanyId
    override lazy val liferayHelper: LiferayHelper = new LiferayHelperTestImpl(
      defaultCompanyId,
      users = Seq(ForcedUserInfo(user1Id, "user 1", "/logo/u1", Nil, Nil, Nil, Nil))
    )
  }

  lazy val statementListener =
    new StatementListener(servlet.dbActions, servlet.taskManager)(servlet.executionContext)


  test("goal status should be InProgress after publish") {

    val lpId = createLearningPath("test lp")
    addMember(lpId, user1Id, MemberTypes.User)
    val goalId = createStatementGoal(lpId, verbId, objectId)
    publish(lpId)

    get(s"/learning-paths/$lpId/members/users/$user1Id/goals-progress/") {
      status should beOk

      body should haveJson(
        s"""[{
           |  "goalId": $goalId,
           |  "status": "InProgress"
           |}]""".stripMargin
      )
    }
  }


  test("goal status should be Success after statement created") {

    val lpId = createLearningPath("test lp")
    val goalId = createStatementGoal(lpId, verbId, objectId)
    addMember(lpId, user1Id, MemberTypes.User)
    publish(lpId)

    await {
      statementListener.onStatementCreated(
        user1Id,
        StatementInfo(verbId, objectId, timeStamp = DateTime.now)
      )(defaultCompanyId)
    }

    get(s"/learning-paths/$lpId/members/users/$user1Id/goals-progress/") {
      status should beOk

      body should haveJson(
        s"""[{
           |  "goalId": $goalId,
           |  "status": "Success"
           |}]""".stripMargin
      )
    }
  }

  test("goal status should be Failed after timeout") {

    val lpId = createLearningPath("test lp")
    val goalId = createStatementGoal(lpId, verbId, objectId, timeLimit = Some(Period.seconds(1)))
    addMember(lpId, user1Id, MemberTypes.User)
    publish(lpId)

    Thread.sleep(1000)

    await {
      statementListener.onStatementCreated(
        user1Id,
        StatementInfo(verbId, objectId, timeStamp = DateTime.now)
      )(defaultCompanyId)
    }

    get(s"/learning-paths/$lpId/members/users/$user1Id/goals-progress/") {
      status should beOk

      body should haveJson(
        s"""[{
           |  "goalId": $goalId,
           |  "status": "Failed"
           |}]""".stripMargin
      )
    }
  }


  test("lp status should be Success after single statement goal complete") {

    val lpId = createLearningPath("test lp")
    val goalId = createStatementGoal(lpId, verbId, objectId)
    addMember(lpId, user1Id, MemberTypes.User)
    publish(lpId)

    await {
      statementListener.onStatementCreated(
        user1Id,
        StatementInfo(verbId, objectId, timeStamp = DateTime.now)
      )(defaultCompanyId)
    }

    get(s"/learning-paths/$lpId/members/users/$user1Id/progress/") {
      status should beOk

      body should haveJson(
        s"""{
           |  "status": "Success"
           |}""".stripMargin
      )
    }
  }

  test("goal group status should be Success after single statement goal complete") {

    val lpId = createLearningPath("test lp")

    val groupId = createGoalGroup(lpId, "group 1")
    val goalId = createStatementGoalInGroup(groupId, verbId, objectId)
    addMember(lpId, user1Id, MemberTypes.User)
    publish(lpId)

    await {
      statementListener.onStatementCreated(
        user1Id,
        StatementInfo(verbId, objectId, timeStamp = DateTime.now)
      )(defaultCompanyId)
    }

    get(s"/learning-paths/$lpId/members/users/$user1Id/goals-progress/") {
      status should beOk

      get(s"/learning-paths/$lpId/members/users/$user1Id/goals-progress/") {
        status should beOk

        body should haveJson(
          s"""[{
             |  "goalId": $groupId,
             |  "status": "Success"
             |},{
             |  "goalId": $goalId,
             |  "status": "Success"
             |}]""".stripMargin
        )
      }
    }
  }

  test("should ignore statement with different verb id") {

    val lpId = createLearningPath("test lp")
    val goalId = createStatementGoal(lpId, verbId, objectId)
    addMember(lpId, user1Id, MemberTypes.User)
    publish(lpId)

    await {
      statementListener.onStatementCreated(
        user1Id,
        StatementInfo(verbId + "_1", objectId, timeStamp = DateTime.now)
      )(defaultCompanyId)
    }

    get(s"/learning-paths/$lpId/members/users/$user1Id/goals-progress/") {
      status should beOk

      body should haveJson(
        s"""[{
           |  "goalId": $goalId,
           |  "status": "InProgress"
           |}]""".stripMargin
      )
    }
  }

  test("should ignore statement with different object id") {

    val lpId = createLearningPath("test lp")
    val goalId = createStatementGoal(lpId, verbId, objectId)
    addMember(lpId, user1Id, MemberTypes.User)
    publish(lpId)

    await {
      statementListener.onStatementCreated(
        user1Id,
        StatementInfo(verbId, objectId + "_1", timeStamp = DateTime.now)
      )(defaultCompanyId)
    }

    get(s"/learning-paths/$lpId/members/users/$user1Id/goals-progress/") {
      status should beOk

      body should haveJson(
        s"""[{
           |  "goalId": $goalId,
           |  "status": "InProgress"
           |}]""".stripMargin
      )
    }
  }

  test("should ignore statement from different user") {

    val lpId = createLearningPath("test lp")
    val goalId = createStatementGoal(lpId, verbId, objectId)
    addMember(lpId, user1Id, MemberTypes.User)
    publish(lpId)

    await {
      statementListener.onStatementCreated(
        userId = user1Id + 1000,
        StatementInfo(verbId, objectId, timeStamp = DateTime.now)
      )(defaultCompanyId)
    }

    get(s"/learning-paths/$lpId/members/users/$user1Id/goals-progress/") {
      status should beOk

      body should haveJson(
        s"""[{
           |  "goalId": $goalId,
           |  "status": "InProgress"
           |}]""".stripMargin
      )
    }
  }
}
