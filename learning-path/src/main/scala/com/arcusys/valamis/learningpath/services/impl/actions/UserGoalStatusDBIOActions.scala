package com.arcusys.valamis.learningpath.services.impl.actions

import com.arcusys.valamis.learningpath.models._
import com.arcusys.valamis.learningpath.services.impl.tables.LearningPathTables
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickProfile
import org.joda.time.{DateTime, Period}
import slick.driver.JdbcProfile

/**
  * Created by mminin on 03/02/2017.
  */
class UserGoalStatusDBIOActions(val profile: JdbcProfile)
  extends LearningPathTables
    with SlickProfile {

  import profile.api._

  def insert(userStatus: UserGoalStatus): DBIO[Int] = {
    userGoalStatusTQ += userStatus
  }

  def insert(userStatus: Seq[UserGoalStatus]): DBIO[Option[Int]] = {
    userGoalStatusTQ ++= userStatus
  }


  private val selectByUserIdQ = Compiled { userId: Rep[Long] =>
    userGoalStatusTQ.filter(_.userId === userId)
  }

  def deleteByUserId(userId: Long): DBIO[Int] = {
    selectByUserIdQ(userId).delete
  }


  private val selectStatusFieldsByUserAndGoalQ = Compiled { (userId: Rep[Long], goalId: Rep[Long]) =>
    userGoalStatusTQ
      .filter(s => s.userId === userId && s.goalId === goalId)
      .map(s => (s.status, s.modifiedDate, s.requiredCount, s.completedCount))
  }

  def updateStatus(userId: Long,
                   goalId: Long,
                   status: GoalStatuses.Value,
                   modifiedDate: DateTime,
                   requiredCount: Int,
                   completedCount: Int): DBIO[Int] = {
    selectStatusFieldsByUserAndGoalQ(userId, goalId)
      .update(status, modifiedDate, requiredCount, completedCount)
  }

  private val getStatusQ = Compiled { (userId: Rep[Long], goalId: Rep[Long]) =>
    userGoalStatusTQ
      .filter(s => s.userId === userId && s.goalId === goalId)
  }

  def getStatus(userId: Long, goalId: Long): DBIO[Option[UserGoalStatus]] = {
    getStatusQ(userId, goalId).result.headOption
  }

  private val getWithGoalByVersionAndStatusQ = Compiled { (versionId: Rep[Long], status: Rep[GoalStatuses.Value]) =>
    goalTQ
      .join(userGoalStatusTQ).on((g, s) => g.id === s.goalId)
      .filter { case (g, s) => g.versionId === versionId && s.status === status }
  }

  def getWithGoalByVersionAndStatus(versionId: Long,
                                    status: GoalStatuses.Value
                                   ): DBIO[Seq[(Goal, UserGoalStatus)]] = {
    getWithGoalByVersionAndStatusQ(versionId, status).result
  }


  private val getWithGoalByStatusUserAndGoalQ = Compiled { (goalId: Rep[Long],
                                                            userId: Rep[Long],
                                                            status: Rep[GoalStatuses.Value]) =>
    goalTQ
      .join(userGoalStatusTQ).on((g, s) => g.id === s.goalId)
      .filter { case (g, s) => g.id === goalId && s.userId === userId && s.status === status }
  }

  def getWithGoalByStatusUserAndGoal(userId: Long,
                                     goalId: Long,
                                     status: GoalStatuses.Value): DBIO[Option[(Goal, UserGoalStatus)]] = {
    getWithGoalByStatusUserAndGoalQ(goalId, userId, status).result.headOption
  }


  private val getByVersionIdQ = Compiled { versionId: Rep[Long] =>
    goalTQ
      .join(userGoalStatusTQ).on((g, s) => g.id === s.goalId)
      .filter { case (g, s) => g.versionId === versionId }
      .map { case (g, s) => s }
  }

  def getByVersion(versionId: Long): DBIO[Seq[UserGoalStatus]] = {
    getByVersionIdQ(versionId).result
  }


  private val selectByUserIdAndVersionIdQ = Compiled { (userId: Rep[Long],
                                                        learningPathId: Rep[Long]) =>
    val goalIdsQ = goalTQ
      .join(versionTQ).on((g, v) => g.versionId === v.id)
      .filter { case (g, v) => v.learningPathId === learningPathId }
      .map { case (g, v) => g.id }

    userGoalStatusTQ
      .filter(s => s.userId === userId && s.goalId.in(goalIdsQ))
  }

  def deleteByUserAndLearningPath(userId: Long, learningPathId: Long): DBIO[Int] = {
    selectByUserIdAndVersionIdQ(userId, learningPathId).delete
  }


  def deleteByUserIdsAndLearningPathId(userIds: Seq[Long],
                                       learningPathId: Long): DBIO[Int] = {
    val goalIdsQ = goalTQ
      .join(versionTQ).on((g, v) => g.versionId === v.id)
      .filter { case (g, v) => v.learningPathId === learningPathId }
      .map { case (g, v) => g.id }

    userIds match {
      case Nil => DBIO.successful(0)
      case ids => userGoalStatusTQ
        .filter(s => (s.userId inSet ids) && s.goalId.in(goalIdsQ))
        .delete
    }
  }


  def deleteByUserIdAndLearningPathIds(userId: Long,
                                       learningPathIds: Seq[Long]): DBIO[Int] = {
    learningPathIds match {
      case Nil => DBIO.successful(0)
      case ids =>
        val goalIdsQ = goalTQ
          .join(versionTQ).on((g, v) => g.versionId === v.id)
          .filter { case (g, v) => v.learningPathId inSet ids }
          .map { case (g, v) => g.id }

        userGoalStatusTQ
        .filter(s => s.userId === userId && s.goalId.in(goalIdsQ))
        .delete
    }
  }


  private val selectUserGoalStatusesCountByGoalIdQ = Compiled { goalId: Rep[Long] =>
    userGoalStatusTQ
      .filter(_.goalId === goalId)
      .groupBy(_.status)
      .map { case (status, group) => (status, group.length) }
  }

  def getUserGoalStatusesCounts(goalId: Long): DBIO[Seq[(GoalStatuses.Value, Int)]] = {
    selectUserGoalStatusesCountByGoalIdQ(goalId).result
  }

  // we need version without join for delete query
  private val selectByVersionIdAndUserIdWithoutJoinQ = Compiled { (versionId: Rep[Long],
                                                                   userId: Rep[Long]) =>
    userGoalStatusTQ
      .filter(_.userId === userId)
      .filter(_.goalId in goalTQ.filter(_.versionId === versionId).map(_.id))
  }

  def deleteByVersionAndUser(versionId: Long, userId: Long): DBIO[Int] = {
    selectByVersionIdAndUserIdWithoutJoinQ(versionId, userId).delete
  }

  private val selectStatusAndDatesByGoalIdQ = Compiled { goalId: Rep[Long] =>
    userGoalStatusTQ
      .filter(_.goalId === goalId)
      .map(g => (g.status, g.startedDate, g.endDate, g.modifiedDate))
  }

  private val selectNotSuccessStatusAndDatesByVersionAndNoLimitQ = Compiled { versionId: Rep[Long] =>
    val goalIdsQ = goalTQ.filter { g =>
      g.versionId === versionId && g.timeLimit.isEmpty
    }.map(_.id)

    userGoalStatusTQ
      .filter(_.status =!= GoalStatuses.Success)
      .filter(_.goalId in goalIdsQ)
      .map(g => (g.status, g.startedDate, g.endDate, g.modifiedDate))
  }

  private val selectNotSuccessWithLimitByVersionIdQ = Compiled { versionId: Rep[Long] =>
    userGoalStatusTQ
      .join(goalTQ).on((s, g) => g.id === s.goalId)
      .filter { case (s, g) =>
        s.status =!= GoalStatuses.Success &&
          g.versionId === versionId &&
          g.timeLimit.isDefined
      }
      .map { case (_, g) =>
        (g.id, g.timeLimit)
      }
  }

  def getNotSuccessWithLimitByVersion(versionId: Long): DBIO[Seq[(Long, Option[Period])]] = {
    selectNotSuccessWithLimitByVersionIdQ(versionId).result
  }


  def updateNotCompletedWithoutLimitByVersion(versionId: Long,
                                              newStatus: GoalStatuses.Value,
                                              startedDate: DateTime,
                                              modifiedDate: DateTime): DBIO[Int] = {
    selectNotSuccessStatusAndDatesByVersionAndNoLimitQ(versionId) update (newStatus, startedDate, None, modifiedDate)
  }

  def updateDatesByGoal(goalId: Long,
                        newStatus: GoalStatuses.Value,
                        startedDate: DateTime,
                        endDate: Option[DateTime],
                        modifiedDate: DateTime): DBIO[Int] = {
    selectStatusAndDatesByGoalIdQ(goalId) update (newStatus, startedDate, endDate, modifiedDate)
  }


  private val selectByLearningPathIdAndUserIdQ = Compiled { (learningPathId: Rep[Long],
                                                             userId: Rep[Long]) =>
    userGoalStatusTQ
      .join(goalTQ).on((s, g) => g.id === s.goalId)
      .join(versionTQ).on { case ((s, g), v) => v.id === g.versionId }
      .filter { case ((s, g), v) => v.learningPathId === learningPathId && s.userId === userId }
      .map { case ((s, _), _) => s }
  }

  def getByLearningPathAndUser(learningPathId: Long, userId: Long): DBIO[Seq[UserGoalStatus]] = {
    selectByLearningPathIdAndUserIdQ(learningPathId, userId).result
  }


  private val getByGoalGroupIdAndUserIdQ = Compiled { (groupId: Rep[Long], userId: Rep[Long]) =>
    goalTQ
      .join(userGoalStatusTQ).on((g, s) => g.id === s.goalId)
      .filter { case (g, s) => g.groupId === groupId && s.userId === userId }
      .map { case (g, s) => s }
  }

  def getByGoalGroupId(groupId: Long, userId: Long): DBIO[Seq[UserGoalStatus]] = {
    getByGoalGroupIdAndUserIdQ(groupId, userId).result
  }

  def getByUserIdAndGoalIds(userId: Long,
                            goalIds: Seq[Long]): DBIO[Seq[UserGoalStatus]] = {
    goalIds match {
      case Nil => DBIO.successful(Nil)
      case ids => userGoalStatusTQ
        .filter(s => (s.goalId inSet ids) && s.userId === userId)
        .result
    }
  }

  private val selectCountByUserAndStatusQ = Compiled { (userId: Rep[Long],
                                                        status: Rep[GoalStatuses.Value]) =>
    userGoalStatusTQ
      .join(goalTQ).on((s, g) => s.goalId === g.id)
      .filter { case (s,g) => s.userId === userId && s.status === status }
      .length
  }

  def getCountByUserAndStatus(userId: Long, status: GoalStatuses.Value): DBIO[Int] = {
    selectCountByUserAndStatusQ(userId, status).result
  }


  private val selectByUserIdsAndLPIdQ = { (userIds: Seq[Long], learningPathId: Long) =>
    val goalIdsQ = goalTQ
      .join(versionTQ).on((g, v) => g.versionId === v.id)
      .filter { case (g, v) => v.learningPathId === learningPathId }
      .map { case (g, v) => g.id }

    userGoalStatusTQ
      .filter(s => (s.userId inSet userIds) && (s.goalId in goalIdsQ))
  }

  def getByUserIdsAndLPId(userIds: Seq[Long], learningPathId: Long): DBIO[Seq[UserGoalStatus]] = {
    selectByUserIdsAndLPIdQ(userIds, learningPathId).result
  }

  private val selectExpiredByVersionIdQ = Compiled { (versionId: Rep[Long], now: Rep[DateTime]) =>
    goalTQ
      .join(userGoalStatusTQ).on((g, s) => g.id === s.goalId)
      .filter { case (g, s) => g.versionId === versionId &&
        s.endDate < now &&
        (s.status === GoalStatuses.InProgress || s.status === GoalStatuses.Undefined)
      }
  }

  def getByExpiredByVersionId(versionId: Long, now: DateTime): DBIO[Seq[(Goal, UserGoalStatus)]] = {
    selectExpiredByVersionIdQ(versionId, now).result
  }


  def getUsersGoalsStatusToCount(userIds: Seq[Long],
                                 learningPathId: Long): DBIO[Seq[(Long, GoalStatuses.Value, Int)]] = {
    val goalIdsQ = goalTQ
      .join(versionTQ).on((g, v) => g.versionId === v.id)
      .filter { case (g, v) =>
        v.learningPathId === learningPathId && g.goalType =!= GoalTypes.Group
      }
      .map { case (g, v) => g.id }

    userGoalStatusTQ
      .filter(s => (s.userId inSet userIds) && (s.goalId in goalIdsQ))
      .groupBy(s => (s.userId, s.status))
      .map { case ((userId, status), statuses) => (userId, status, statuses.length) }
      .result
  }
}
