package com.arcusys.valamis.learningpath.services.impl.actions

import com.arcusys.valamis.learningpath.models.{CertificateStatuses, GoalStatuses, UserLPStatus}
import com.arcusys.valamis.learningpath.services.impl.tables.LearningPathTables
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickProfile
import org.joda.time.DateTime
import slick.driver.JdbcProfile

/**
  * Created by mminin on 13/03/2017.
  */
class UserLPStatusDBIOActions(val profile: JdbcProfile)
  extends LearningPathTables
    with SlickProfile {

  import profile.api._

  private val selectByLearningPathQ = Compiled { (learningPathId: Rep[Long]) =>
    userLPStatusTQ
      .filter(s => s.learningPathId === learningPathId)
  }

  private val selectByUserAndLearningPathQ = Compiled { (learningPathId: Rep[Long],
                                                         userId: Rep[Long]) =>
    userLPStatusTQ
      .filter(s => s.userId === userId && s.learningPathId === learningPathId)
  }

  private val getUserIdsByLearningPathQ = Compiled { (learningPathId: Rep[Long]) =>
    userLPStatusTQ
      .filter(s => s.learningPathId === learningPathId)
      .map(_.userId)
  }


  private val selectByUserIdQ = Compiled { userId: Rep[Long] =>
    userLPStatusTQ.filter(_.userId === userId)
  }

  private val selectStatusesCountByLearningPathIdQ = Compiled { learningPathId: Rep[Long] =>
    userLPStatusTQ
      .filter(_.learningPathId === learningPathId)
      .groupBy(_.status)
      .map { case (status, group) => (status, group.length) }
  }

  def getStatusesCounts(learningPathId: Long, userIds: Seq[Long]): DBIO[Seq[(CertificateStatuses.Value, Int)]] = {
    userLPStatusTQ
      .filter(s => s.learningPathId === learningPathId && (s.userId inSet userIds))
      .groupBy(_.status)
      .map { case (status, group) => (status, group.length) }
      .result
  }

  private val selectLPCountByLearningPathIdQ = Compiled { (startDate: Rep[DateTime],
                                                           endDate: Rep[DateTime],
                                                           companyId: Rep[Long]) =>
    userLPStatusTQ
      .join(learningPathTQ).on((u, lp) => lp.id === u.learningPathId && lp.id === u.learningPathId)
      .filter { case (_, lp) => lp.companyId === companyId }
      .filter { case (u, _) => u.status === CertificateStatuses.Success }
      .filter { case (u, _) => u.modifiedDate >= startDate }
      .filter { case (u, _) => u.modifiedDate <= endDate }
      .groupBy { case (u, _) => u.userId }
      .map { case (userId, group) => (userId, group.length) }
  }

  def getUsersIdsToLPCount(startDate: DateTime,
                           endDate: DateTime,
                           companyId: Long): DBIO[Seq[(Long, Int)]] = {
    selectLPCountByLearningPathIdQ(startDate, endDate, companyId).result
  }

  def insert(userStatus: Seq[UserLPStatus]): DBIO[Option[Int]] = {
    userLPStatusTQ ++= userStatus
  }

  def deleteByUserId(userId: Long): DBIO[Int] = {
    selectByUserIdQ(userId).delete
  }

  def getByUserId(userId: Long): DBIO[Seq[UserLPStatus]] = {
    selectByUserIdQ(userId).result
  }

  def deleteByLearningPath(learningPathId: Long) = {
    selectByLearningPathQ(learningPathId).delete
  }

  def getByLearningPathId(learningPathId: Long): DBIO[Seq[UserLPStatus]] = {
    selectByLearningPathQ(learningPathId).result
  }

  def getByUserAndLearningPath(learningPathId: Long,
                               userId: Long): DBIO[Option[UserLPStatus]] = {
    selectByUserAndLearningPathQ(learningPathId, userId).result.headOption
  }

  def deleteByUserAndLearningPath(learningPathId: Long,
                                  userId: Long): DBIO[Int] = {
    selectByUserAndLearningPathQ(learningPathId, userId).delete
  }

  def getUserIdsByLearningPath(learningPathId: Long): DBIO[Seq[Long]] = {
    getUserIdsByLearningPathQ(learningPathId).result
  }


  private val selectStatusByUserAndLearningPathQ = Compiled {
    (learningPathId: Rep[Long],
     userId: Rep[Long]) =>
      userLPStatusTQ
        .filter(s => s.userId === userId && s.learningPathId === learningPathId)
        .map(s => (s.status, s.modifiedDate, s.progress))
  }

  def updateStatus(status: UserLPStatus): DBIO[Int] = {
    selectStatusByUserAndLearningPathQ(status.learningPathId, status.userId)
      .update(status.status, status.modifiedDate, status.progress)
  }


  private val selectNotSuccessStatusAndDatesByVersionQ = Compiled { versionId: Rep[Long] =>
    userLPStatusTQ
      .filter(s => s.versionId === versionId && s.status =!= CertificateStatuses.Success)
      .map(s => (s.status, s.startedDate, s.modifiedDate))
  }

  def updateNotCompletedByVersion(versionId: Long,
                                  newStatus: CertificateStatuses.Value,
                                  startedDate: DateTime,
                                  modifiedDate: DateTime): DBIO[Int] = {
    selectNotSuccessStatusAndDatesByVersionQ(versionId)
      .update((newStatus, startedDate, modifiedDate))
  }


  private val selectByVersionAndStatusQ = Compiled { (versionId: Rep[Long],
                                                      status: Rep[CertificateStatuses.Value]) =>
    userLPStatusTQ
      .filter(s => s.versionId === versionId && s.status === status)
  }

  def getByVersionAndStatus(versionId: Long,
                            status: CertificateStatuses.Value): DBIO[Seq[UserLPStatus]] = {
    selectByVersionAndStatusQ(versionId, status).result
  }


  private val selectVersionIdByLpIdAndUserIdQ = Compiled { (lpId: Rep[Long], userId: Rep[Long]) =>
    userLPStatusTQ
      .filter(s => s.learningPathId === lpId && s.userId === userId)
      .map(s => s.versionId)
  }

  def updateVersionIdByLpIdAndUserId(learningPathId: Long,
                                     userId: Long,
                                     newVersionId: Long): DBIO[Int] = {
    selectVersionIdByLpIdAndUserIdQ(learningPathId, userId) update newVersionId
  }


  def getByUserIdsAndLearningPathId(userIds: Seq[Long],
                                    learningPathId: Long): DBIO[Seq[UserLPStatus]] = {
    userIds match {
      case Nil => DBIO.successful(Nil)
      case ids => userLPStatusTQ
        .filter(s => (s.userId inSet ids) && s.learningPathId === learningPathId)
        .result
    }
  }

  def getByUserIdAndLearningPathIds(userId: Long,
                                    learningPathIds: Seq[Long]): DBIO[Seq[UserLPStatus]] = {
    learningPathIds match {
      case Nil => DBIO.successful(Nil)
      case ids => userLPStatusTQ
        .filter(s => s.userId === userId && (s.learningPathId inSet ids))
        .result
    }
  }


  def deleteByUserIdsAndLearningPathId(userIds: Seq[Long],
                                       learningPathId: Long): DBIO[Int] = {
    userIds match {
      case Nil => DBIO.successful(0)
      case ids => userLPStatusTQ
        .filter(s => (s.userId inSet ids) && s.learningPathId === learningPathId)
        .delete
    }
  }

  def deleteByUserIdAndLearningPathIds(userId: Long,
                                       learningPathIds: Seq[Long]): DBIO[Int] = {
    learningPathIds match {
      case Nil => DBIO.successful(0)
      case ids => userLPStatusTQ
        .filter(s => s.userId === userId && (s.learningPathId inSet ids))
        .delete
    }
  }

  private val selectStatusToCountByLPIdQ = { learningPathIds: Seq[Long] =>
    userLPStatusTQ
      .filter(_.learningPathId inSet learningPathIds)
      .groupBy(s => (s.learningPathId, s.status))
      .map { case ((lpId, status), statuses) => (lpId, status, statuses.length) }
  }

  def getGoalsStatusToCount(learningPathIds: Seq[Long]): DBIO[Seq[(Long, CertificateStatuses.Value, Int)]] = {
    selectStatusToCountByLPIdQ(learningPathIds)
      .result
  }

  // TODO make a compiled query
  def getUserIds()(implicit companyId: Long): DBIO[Seq[Long]] = {
    userLPStatusTQ
      .join(learningPathTQ).on((u, lp) => lp.id === u.learningPathId && lp.companyId === companyId)
      .join(versionTQ).on((u, v) => v.id === u._2.currentVersionId)
      .groupBy(x => x._1._1.userId).map(_._1).result
  }

  // TODO make a compiled query
  def getUserIds(courseId: Long)(implicit companyId: Long): DBIO[Seq[Long]] = {
    userLPStatusTQ
      .join(learningPathTQ).on((u, lp) => lp.id === u.learningPathId && lp.companyId === companyId)
      .join(versionTQ).on((tuple, v) => v.id === tuple._2.currentVersionId && v.courseId === courseId)
      .groupBy(x => x._1._1.userId).map(_._1).result
  }
}