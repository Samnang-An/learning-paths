package com.arcusys.valamis.learningpath.services.impl

import com.arcusys.valamis.learningpath.models._
import com.arcusys.valamis.learningpath.services.{CertificateNotificationService, LPStatementService}
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickProfile
import com.arcusys.valamis.learningpath.services.impl.tables.{LPMemberTableComponent, LeaningPathTableComponent}
import com.arcusys.valamis.learningpath.services.impl.utils.UserStatusUtil
import com.arcusys.valamis.learningpath.tasks.TaskManager
import com.arcusys.valamis.learningpath.utils.DbActions
import com.arcusys.valamis.members.picker.model.MemberTypes.MemberType
import com.arcusys.valamis.members.picker.model.{Member, MemberTypes, UserMembership}
import com.arcusys.valamis.members.picker.service.MemberService
import org.joda.time.DateTime

import scala.concurrent.Future

/**
  * add members overrides
  * after adding users we need to create user status for published learning path
  * and run checkers
  */
trait LPMemberServiceAddComponent {
  self: MemberService
    with LPMemberTableComponent
    with LeaningPathTableComponent
    with SlickProfile =>

  import profile.api._


  def taskManager: TaskManager
  def actions: DbActions
  def userLPStatusModelListener: UserLPStatusModelListener
  def certificateNotificationService: CertificateNotificationService
  def lpStatementService: LPStatementService

  override def addMembers(members: Seq[Member]): Future[Unit] = {
    implicit val companyId = liferay.getCompanyId

    val lpId = members.map(_.entityId).distinct match {
      case Seq(id) => id
      case _ => ??? //add many members to different learning paths not supported yet
    }

    val users = members flatMap { member =>
      liferay.getMemberUserIds(member) map { userId =>
        UserMembership(userId, member.id, member.tpe, member.entityId)
      }
    }
    val userIds = users.map(_.userId).distinct
    val dbAction = for {
      _ <- addMembersAction(members)
      _ <- addUserMembershipsAction(users)
      newStatuses <- addUsersProgressIfPublished(lpId, userIds)
      lp <- actions.learningPathDBIO.getById(lpId).map(_.get)
      (versionId, version) <- actions.versionDBIO.getById(lp.currentVersionId.get).map(_.get)
    } yield {
      (lp, newStatuses, version)
    }

    db.run {
      dbAction.transactionally
    } flatMap { case (lp, newStatuses, version) =>
      if (lp.activated) taskManager.planUndefinedStatusChecker(lp.id)

      userLPStatusModelListener.onCreated(newStatuses) flatMap { _ =>
        val userIds = newStatuses.map(_.userId)
        lpStatementService.sendStatementAddedUser(userIds, companyId, version)
        certificateNotificationService.sendUsersAddedNotification(lp.id, userIds)
      }
    }
  }

  override def addMember(member: Member): Future[Unit] = {
    implicit val companyId = liferay.getCompanyId

    val lpId = member.entityId
    val userIds = liferay.getMemberUserIds(member)
    val users = userIds.map(UserMembership(_, lpId, member.tpe, member.entityId))

    val dbAction = for {
      _ <- addMemberAction(member)
      _ <- addUserMembershipsAction(users)
      newStatuses <- addUsersProgressIfPublished(lpId, userIds)
      lp <- actions.learningPathDBIO.getById(lpId).map(_.get)
    } yield {
      (lp, newStatuses)
    }

    db.run {
      dbAction.transactionally
    } flatMap { case (lp, newStatuses) =>
      if (lp.activated) taskManager.planUndefinedStatusChecker(lp.id)

      userLPStatusModelListener.onCreated(newStatuses)
    }
  }

  override def addUserAsGroupMember(userId: Long,
                                    groupId: Long,
                                    groupType: MemberType): Future[Unit] = {
    implicit val companyId = liferay.getCompanyId

    val dbAction = for {
      learningPathIds <- getEntityIdsWithMember(groupId, groupType)
      _ <- addUserMembershipsAction(learningPathIds.map {
        UserMembership(userId, groupId, groupType, _)
      })
      newStatuses <- DBIO.sequence(learningPathIds.map(addUsersProgressIfPublished(_, Seq(userId))))
    } yield {
      (learningPathIds, newStatuses.flatten)
    }

    db.run {
      dbAction.transactionally
    } flatMap { case (learningPathIds, newStatuses) =>
      learningPathIds.foreach( taskManager.planUndefinedStatusChecker )

      userLPStatusModelListener.onCreated(newStatuses)
    }
  }


  private def addUsersProgressIfPublished(learningPathId: Long,
                                          newUserIds: Seq[Long]): DBIO[Seq[UserLPStatus]] = {

    actions.versionDBIO.getCurrentByLearningPathId(learningPathId).flatMap {
      case Some((versionId, version)) if version.published =>
        addUsersProgress(version.learningPathId, versionId, newUserIds)
      case _ => DBIO.successful(Nil)
    }
  }

  private def addUsersProgress(learningPathId: Long,
                               versionId: Long,
                               newUserIds: Seq[Long]): DBIO[Seq[UserLPStatus]] = {
    val now = DateTime.now()
    val lpStatus = CertificateStatuses.InProgress

    actions.userLPStatusDBIO.getUserIdsByLearningPath(learningPathId)
      .flatMap { oldUserIds =>
        val userIds = newUserIds diff oldUserIds

        val progress = 0 //progress will be updated after check goals
        val userLPStatuses = userIds.map { userId =>
          UserLPStatus(userId, learningPathId, versionId, lpStatus, now, now, progress)
        }

        for {
          _ <- actions.userLPStatusDBIO.insert(userLPStatuses)
          goals <- actions.goalDBIO.getByVersionId(versionId)
          _ <- actions.userGoalStatusDBIO.insert(
            UserStatusUtil.getNewStatuses(goals, userIds, now)
          )
        } yield {
          userLPStatuses
        }
      }
  }
}
