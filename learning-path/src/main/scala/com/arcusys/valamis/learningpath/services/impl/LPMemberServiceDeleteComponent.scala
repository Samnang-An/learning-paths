package com.arcusys.valamis.learningpath.services.impl

import com.arcusys.valamis.learningpath.models.UserLPStatus
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickProfile
import com.arcusys.valamis.learningpath.services.impl.tables.{LPMemberTableComponent, LeaningPathTableComponent}
import com.arcusys.valamis.learningpath.tasks.TaskManager
import com.arcusys.valamis.learningpath.utils.DbActions
import com.arcusys.valamis.members.picker.model.MemberTypes.MemberType
import com.arcusys.valamis.members.picker.model.{Member, MemberTypes}
import com.arcusys.valamis.members.picker.service.MemberService

import scala.concurrent.Future
import scala.language.postfixOps

/**
  * delete members overrides
  * after deleting users we need to delete user status
  */
trait LPMemberServiceDeleteComponent {
  self: MemberService
    with LPMemberTableComponent
    with LeaningPathTableComponent
    with SlickProfile =>

  import profile.api._

  def taskManager: TaskManager
  def actions: DbActions
  def userLPStatusModelListener: UserLPStatusModelListener

  override def deleteMembers(entityId: Long, tpe: MemberType, memberIds: Seq[Long]): Future[Unit] = {
    //not used in LP
    // TODO: add user status cleaner before restore
    ???
  }

  override def deleteUsers(entityId: Long, userIds: Seq[Long]): Future[Unit] = {
    //not used in LP
    // TODO: add user status cleaner before restore
    ???
  }


  /**
    * delete user from all learning paths
    */
  override def deleteUser(userId: Long): Future[Unit] = {
    val action = for {
      userStatuses <- actions.userLPStatusDBIO.getByUserId(userId)
      _ <- deleteMemberAction(userId, MemberTypes.User)
      _ <- deleteUserAction(userId)
      _ <- deleteUserLPStatusesAction(userId)
      _ <- deleteUserGoalStatusesAction(userId)
    } yield {
      userStatuses
    }
    db.run { action transactionally}
      .flatMap { userStatuses =>
      userLPStatusModelListener.onDeleted(userStatuses)
    }
  }

  /**
    * delete user from learning path
    */
  override def deleteUser(entityId: Long, userId: Long): Future[Unit] = {
    val learningPathId = entityId

    val action = for {
      userStatuses <- actions.userLPStatusDBIO.getByUserAndLearningPath(learningPathId, userId)
      _ <- deleteMemberAction(Member(userId, MemberTypes.User, entityId))
      _ <- deleteUserAction(entityId, userId)
      _ <- deleteUserLPStatusAction(userId, learningPathId)
      _ <- deleteUserGoalStatusesAction(userId, learningPathId)
    } yield {
      userStatuses
    }
    db.run {action transactionally}
      .flatMap {
        case Some(userStatus) => userLPStatusModelListener.onDeleted(userStatus)
        case None => Future.successful{}
    }
  }

  /**
    * delete member(user or group) from learning path.
    * user could stay in learning path like other type member
    */
  override def deleteMember(member: Member): Future[Unit] = {
    val learningPathId = member.entityId

    val action = for {
      affectedUserIds <- actions.userMemberDBIO
        .getByLearningPathIdAndGroupId(learningPathId, member.id)
        .map(_.map(_.userId).distinct)
      _ <- deleteMemberAction(member)
      _ <- deleteUserMembershipAction(member)
      removeUserLPStatuses <- checkAndDeleteUnusedUsersStatuses(learningPathId, affectedUserIds)
    } yield {
      removeUserLPStatuses
    }

    db.run(action.transactionally) flatMap { removeUserLPStatuses =>
      userLPStatusModelListener.onDeleted(removeUserLPStatuses)
    }
  }

  /**
    * delete user like group member, user is no longer in the group.
    * user could stay in learning path like other type member
    */
  override def deleteUserAsGroupMember(userId: Long,
                                       groupId: Long,
                                       groupType: MemberType): Future[Unit] = {
    val action = for {
      learningPathIds <- actions.userMemberDBIO.getByUserIdAndGroupId(userId, groupId)
        .map(_.map(_.entityId).distinct)
      _ <- deleteUserMembershipsAction(userId, groupId, groupType)
      removeUserLPStatuses <- checkAndDeleteUnusedUsersStatuses(learningPathIds, userId)
    } yield {
      removeUserLPStatuses
    }

    db.run(action.transactionally) flatMap { removeUserLPStatuses =>
      userLPStatusModelListener.onDeleted(removeUserLPStatuses)
    }
  }

  private def deleteUserLPStatusesAction(userId: Long): DBIO[Unit] = {
    actions.userLPStatusDBIO.deleteByUserId(userId)
      .map(_ => {})
  }

  private def deleteUserLPStatusAction(userId: Long, learningPathId: Long): DBIO[Unit] = {
    actions.userLPStatusDBIO.deleteByUserAndLearningPath(learningPathId, userId)
      .map(_ => {})
  }

  private def deleteUserGoalStatusesAction(userId: Long): DBIO[Unit] = {
    actions.userGoalStatusDBIO.deleteByUserId(userId)
      .map(_ => {})
  }

  private def deleteUserGoalStatusesAction(userId: Long, learningPathId: Long): DBIO[Unit] = {
    actions.userGoalStatusDBIO.deleteByUserAndLearningPath(userId, learningPathId)
      .map(_ => {})
  }


  private def checkAndDeleteUnusedUsersStatuses(learningPathId: Long,
                                                userIds: Seq[Long]): DBIO[Seq[UserLPStatus]] = {

    actions.userMemberDBIO.getByUserIdsAndLearningPathId(userIds, learningPathId)
      .flatMap { members =>
        val memberIds = members.map(_.memberId)
        val removedUserIds = userIds diff memberIds

        for {
          userStatuses <- actions.userLPStatusDBIO.getByUserIdsAndLearningPathId(removedUserIds, learningPathId)
          _ <- actions.userLPStatusDBIO.deleteByUserIdsAndLearningPathId(removedUserIds, learningPathId)
          _ <- actions.userGoalStatusDBIO.deleteByUserIdsAndLearningPathId(removedUserIds, learningPathId)
        } yield {
          userStatuses
        }
      }
  }

  private def checkAndDeleteUnusedUsersStatuses(learningPathIds: Seq[Long],
                                                userId: Long): DBIO[Seq[UserLPStatus]] = {

    actions.userMemberDBIO.getByUserIdAndLearningPathIds(userId, learningPathIds)
      .flatMap { members =>
        val lpIds = members.map(_.entityId)
        val removedLpIds = learningPathIds diff lpIds

        for {
          userStatuses <- actions.userLPStatusDBIO.getByUserIdAndLearningPathIds(userId, removedLpIds)
          _ <- actions.userLPStatusDBIO.deleteByUserIdAndLearningPathIds(userId, removedLpIds)
          _ <- actions.userGoalStatusDBIO.deleteByUserIdAndLearningPathIds(userId, removedLpIds)
        } yield {
          userStatuses
        }
      }
  }
}
