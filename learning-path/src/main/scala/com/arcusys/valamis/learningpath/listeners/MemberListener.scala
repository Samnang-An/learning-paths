package com.arcusys.valamis.learningpath.listeners

import com.arcusys.valamis.members.picker.model.MemberTypes
import com.arcusys.valamis.members.picker.service.MemberService
import slick.jdbc.JdbcBackend

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by mminin on 28/02/2017.
  */
class MemberListener(db: JdbcBackend#DatabaseDef,
                     memberService: MemberService)
                    (implicit executionContext: ExecutionContext) {


  //TODO: add logs, create tests
  def onUserRemoved(userId: Long): Future[Unit] = {
    memberService.deleteUser(userId)
  }

  def onUserJoinGroup(userId: Long, groupId: Long, groupType: MemberTypes.Value): Future[Unit] = {
    memberService.addUserAsGroupMember(userId, groupId, groupType)
  }

  def onUserLeaveGroup(userId: Long, groupId: Long, groupType: MemberTypes.Value): Future[Unit] = {
    memberService.deleteUserAsGroupMember(userId, groupId, groupType)
  }
}
