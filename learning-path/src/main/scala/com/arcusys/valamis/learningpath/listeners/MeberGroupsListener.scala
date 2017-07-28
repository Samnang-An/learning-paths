package com.arcusys.valamis.learningpath.listeners

import com.arcusys.valamis.learningpath.utils.{DbActions, DbActionsSupport}
import com.arcusys.valamis.members.picker.model.{Member, MemberTypes}
import com.arcusys.valamis.members.picker.service.MemberService

import scala.concurrent.{ExecutionContext, Future}

class MeberGroupsListener(val dbActions: DbActions,
                          memberService: MemberService)
                         (implicit executionContext: ExecutionContext) extends DbActionsSupport {

  def onRemoved(memberId: Long, tpe: MemberTypes.Value)
               (implicit companyId: Long): Future[Unit] = {
    db.run {
      memberDBIO.getLPIdByMember(memberId, tpe)
    } flatMap { lpIds =>
      Future.sequence(
        lpIds
          .map(Member(memberId, tpe, _))
          .map(memberService.deleteMember)
      )
    } map {
      _ => Unit
    }
  }
}
