package com.arcusys.valamis.learningpath.web.servlets.goals.checkers

import com.arcusys.valamis.learningpath.models.{GoalStatuses, GoalTypes, UserGoalStatus}
import com.arcusys.valamis.learningpath.tasks.{DeadlineChecker, ExpiredGoalsChecker}
import com.arcusys.valamis.learningpath.{LiferayHelperTestImpl, ServletImpl}
import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase
import com.arcusys.valamis.members.picker.model.MemberTypes
import com.arcusys.valamis.members.picker.model.impl.ForcedUserInfo
import com.arcusys.valamis.members.picker.service.LiferayHelper
import org.joda.time.{DateTime, Period}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

/**
  * Created by pkornilov on 4/6/17.
  */
class DeadlineCheckerTest extends LPServletTestBase {
  self =>

  private var now = () => DateTime.now

  private val user1Id = 48151L
  private val user2Id = 62342L

  implicit private val execContext = servlet.executionContext
  private lazy val expiredGoalChecker = new ExpiredGoalsChecker(servlet.dbActions, servlet.taskManager)
  private lazy val deadlineChecker = new DeadlineChecker(servlet.dbActions, expiredGoalChecker)

  override lazy val servlet: ServletImpl = new ServletImpl(dbInfo) {

    override def now: DateTime = self.now()

    override def liferayHelper: LiferayHelper = new LiferayHelperTestImpl(
      companyId,
      users = Seq(
        ForcedUserInfo(user1Id, "user 1", "/logo/u1", Nil, Nil, Nil, Nil),
        ForcedUserInfo(user2Id, "user 2", "/logo/u1", Nil, Nil, Nil, Nil)
      ))
  }

  test("should correctly set end date after first publish") {
    val lpId = createLearningPath("deadline test")

    val goal1Id = createActivityGoal(lpId, timeLimit = None)
    val goal2Id = createActivityGoal(lpId, timeLimit = Some(Period.days(2)))
    val goal3Id = createActivityGoal(lpId, timeLimit = Some(Period.months(1)))

    addMember(lpId, user1Id, MemberTypes.User)
    publish(lpId)

    val goalStatuses = getUserGoalStatuses(lpId, user1Id).sortBy(_.goalId)
    val startDate = goalStatuses.head.startedDate
    val modifiedDate = goalStatuses.head.modifiedDate

    goalStatuses shouldBe List(
      UserGoalStatus(user1Id, goal1Id, GoalStatuses.InProgress, startDate, modifiedDate, 1, 0,
        None),
      UserGoalStatus(user1Id, goal2Id, GoalStatuses.InProgress, startDate, modifiedDate, 1, 0,
        Some(startDate.withPeriodAdded(Period.days(2), 1))),
      UserGoalStatus(user1Id, goal3Id, GoalStatuses.InProgress, startDate, modifiedDate, 1, 0,
        Some(startDate.withPeriodAdded(Period.months(1), 1)))
    )

  }

  test("should correctly change dates after publish new version") {
    val lpId = createLearningPath("deadline test")

    val goal1Id = createActivityGoal(lpId, timeLimit = None)
    val goal2Id = createActivityGoal(lpId, timeLimit = Some(Period.days(2)))
    val goal3Id = createActivityGoal(lpId, timeLimit = Some(Period.months(1)))

    addMember(lpId, user1Id, MemberTypes.User)
    publish(lpId)
    val initGoalStatuses = getUserGoalStatuses(lpId, user1Id)
    val initStartDate = initGoalStatuses.head.startedDate
    val initModifiedDate = initGoalStatuses.head.modifiedDate

    createNewDraft(lpId)
    val goalIdMap = getOldToNewGoalMap(lpId)
    val goal1NewId = goalIdMap(goal1Id)
    val goal2NewId = goalIdMap(goal2Id)
    val goal3NewId = goalIdMap(goal3Id)

    updateGoal(goal1NewId, GoalTypes.LRActivity, timeLimit = Some(Period.days(1)))
    updateGoal(goal2NewId, GoalTypes.LRActivity, timeLimit = None)
    updateGoal(goal3NewId, GoalTypes.LRActivity, timeLimit = Some(Period.weeks(2)))

    Thread.sleep(1500)
    publish(lpId)

    val goalStatuses = getUserGoalStatuses(lpId, user1Id).sortBy(_.goalId)
    val startDate = goalStatuses.head.startedDate
    val modifiedDate = goalStatuses.head.modifiedDate

    assert(startDate === initStartDate, "startDate should NOT change after republish")
    assert(modifiedDate !== initModifiedDate, "modified date should change after republish")

    goalStatuses shouldBe List(
      UserGoalStatus(user1Id, goal1NewId, GoalStatuses.InProgress, initStartDate, modifiedDate, 1, 0,
        Some(startDate.withPeriodAdded(Period.days(1), 1))), //limit was added
      UserGoalStatus(user1Id, goal2NewId, GoalStatuses.InProgress, initStartDate, modifiedDate, 1, 0,
        None), //limit was removed
      UserGoalStatus(user1Id, goal3NewId, GoalStatuses.InProgress, initStartDate, modifiedDate, 1, 0,
        Some(startDate.withPeriodAdded(Period.weeks(2), 1))) //limit was changed
    )

  }

  test("should correctly change dates after reactivation") {
    val lpId = createLearningPath("deadline test")

    val goal1Id = createActivityGoal(lpId, timeLimit = None)
    val goal2Id = createActivityGoal(lpId, timeLimit = Some(Period.days(2)))
    val goal3Id = createActivityGoal(lpId, timeLimit = Some(Period.months(1)))

    addMember(lpId, user1Id, MemberTypes.User)
    publish(lpId)
    val initGoalStatuses = getUserGoalStatuses(lpId, user1Id)
    val initStartDate = initGoalStatuses.head.startedDate
    val initModifiedDate = initGoalStatuses.head.modifiedDate

    deactivate(lpId)
    Thread.sleep(1500) //TODO get rid of sleep
    activate(lpId)

    val goalStatuses = getUserGoalStatuses(lpId, user1Id).sortBy(_.goalId)
    val startDate = goalStatuses.head.startedDate
    val modifiedDate = goalStatuses.head.modifiedDate

    assert(startDate !== initStartDate, "startDate should change after reactivation")
    assert(modifiedDate !== initModifiedDate, "modified date should change after reactivation")

    goalStatuses.length shouldBe 3

    goalStatuses shouldBe List(
      UserGoalStatus(user1Id,
        goal1Id,
        GoalStatuses.InProgress,
        goalStatuses(0).startedDate,
        goalStatuses(0).modifiedDate,
        1,
        0,
        None),
      UserGoalStatus(user1Id,
        goal2Id,
        GoalStatuses.InProgress,
        goalStatuses(1).startedDate,
        goalStatuses(1).modifiedDate,
        1,
        0,
        Some(startDate.withPeriodAdded(Period.days(2), 1))),
      UserGoalStatus(user1Id,
        goal3Id,
        GoalStatuses.InProgress,
        goalStatuses(2).startedDate,
        goalStatuses(2).modifiedDate,
        1,
        0,
        Some(startDate.withPeriodAdded(Period.months(1), 1)))
    )

  }

  test("should set Failed status for expired goals") {
    val lpId = createLearningPath("deadline test")

    val goal1Id = createActivityGoal(lpId, timeLimit = None)
    val goal2Id = createActivityGoal(lpId, timeLimit = Some(Period.days(2)))
    val goal3Id = createActivityGoal(lpId, timeLimit = Some(Period.months(1)), activityName = "success")

    addMember(lpId, user1Id, MemberTypes.User)
    publish(lpId)

    val after3Days = DateTime.now.withPeriodAdded(Period.days(3), 1).withMillisOfSecond(0)

    now = () => after3Days

    userCreatesNewLRActivity(servlet, user1Id, activityType = "success")(-1)

    await(deadlineChecker.checkDeadlines(now()))

    val goalStatuses = getUserGoalStatuses(lpId, user1Id).sortBy(_.goalId)
    val startDate = goalStatuses.head.startedDate
    val modifiedDate = goalStatuses.head.modifiedDate
    val failedEndDate = startDate.withPeriodAdded(Period.days(2), 1)

    now = () => DateTime.now //for normal work of other tests

    goalStatuses shouldBe List(
      UserGoalStatus(user1Id, goal1Id, GoalStatuses.InProgress, startDate, modifiedDate, 1, 0,
        None),
      UserGoalStatus(user1Id, goal2Id, GoalStatuses.Failed, startDate, failedEndDate, 1, 0,
        Some(failedEndDate)),
      UserGoalStatus(user1Id, goal3Id, GoalStatuses.Success, startDate, after3Days, 1, 1,
        Some(startDate.withPeriodAdded(Period.months(1), 1)))
    )


  }


  private def getOldToNewGoalMap(lpId: Long): Map[Long, Long] = await {
    val dbActions = servlet.dbActions
    db.run(for {
      version <- dbActions.versionDBIO.getDraftByLearningPathId(lpId)
      goals <- version match {
        case Some((versionId, _)) => dbActions.goalDBIO.getByVersionId(versionId)
        case _ => throw new NoSuchElementException(s"no version for lp $lpId")
      }
    } yield goals map (g => (g.oldGoalId.get, g.id)) toMap)
  }


}
