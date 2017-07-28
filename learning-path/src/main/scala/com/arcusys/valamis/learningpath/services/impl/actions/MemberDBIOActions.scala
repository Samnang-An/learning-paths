package com.arcusys.valamis.learningpath.services.impl.actions

import com.arcusys.valamis.learningpath.services.impl.tables.LearningPathTables
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickProfile
import com.arcusys.valamis.members.picker.model.MemberTypes
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

class MemberDBIOActions(val profile: JdbcProfile)
                       (implicit executionContext: ExecutionContext)
  extends LearningPathTables
    with SlickProfile {

  import profile.api._

  private val selectByEntityIdQ = Compiled { entityId: Rep[Long] =>
    membersTQ
      .filter(_.entityId === entityId)
  }

  def deleteByLearningPathId(learningPathId: Long): DBIO[Int] = {
    selectByEntityIdQ(learningPathId).delete
  }


  private val selectEntityIdByMemberQ = Compiled { (memberId: Rep[Long],
                                                    tpe: Rep[MemberTypes.Value],
                                                    companyId: Rep[Long]) =>

    learningPathTQ
      .join(membersTQ).on((lp, m) => lp.id === m.entityId)
      .filter { case (lp, m) =>
        lp.companyId === companyId &&
          m.memberId === memberId &&
          m.memberType === tpe
      }
      .map { case (lp, m) => m.entityId }
  }

  def getLPIdByMember(memberId: Long,
                      tpe: MemberTypes.Value)
                     (implicit companyId: Long): DBIO[Seq[Long]] = {
    selectEntityIdByMemberQ(memberId, tpe, companyId).result
  }
}
