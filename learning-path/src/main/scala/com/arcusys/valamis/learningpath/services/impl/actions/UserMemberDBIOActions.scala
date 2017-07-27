package com.arcusys.valamis.learningpath.services.impl.actions

import com.arcusys.valamis.learningpath.services.impl.tables.LearningPathTables
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickProfile
import com.arcusys.valamis.members.picker.model.UserMembership
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

/**
  * Created by mminin on 26/01/2017.
  */
class UserMemberDBIOActions(val profile: JdbcProfile)
                           (implicit executionContext: ExecutionContext)
  extends LearningPathTables
    with SlickProfile {

  import profile.api._

  private val selectByEntityIdQ = Compiled { entityId: Rep[Long] =>
    usersMembershipTQ
      .filter(_.entityId === entityId)
  }

  def deleteByLearningPathId(learningPathId: Long): DBIO[Int] = {
    selectByEntityIdQ(learningPathId).delete
  }

  private val selectUserIdByEntityIdQ = Compiled { entityId: Rep[Long] =>
    usersMembershipTQ
      .filter(_.entityId === entityId)
      .map(_.userId)
  }

  //TODO: implement distinct on db side
  def getUserIdsByLearningPathId(learningPathId: Long): DBIO[Seq[Long]] = {
    selectUserIdByEntityIdQ(learningPathId).result
      .map(_.distinct)
  }


  private val selectByLearningPathIdAndGroupIdQ = Compiled { (learningPathId: Rep[Long],
                                                              groupId: Rep[Long]) =>
    usersMembershipTQ
      .filter(m => m.entityId === learningPathId && m.memberId === groupId)
  }

  def getByLearningPathIdAndGroupId(learningPathId: Long, groupId: Long): DBIO[Seq[UserMembership]] = {
    selectByLearningPathIdAndGroupIdQ(learningPathId, groupId).result
  }


  private val selectByUserIdAndLearningPathIdQ = Compiled { (userId: Rep[Long],
                                                            learningPathId: Rep[Long]) =>
    usersMembershipTQ
      .filter(m => m.userId === userId && m.entityId === learningPathId)
  }

  def getByUserIdAndLearningPathId(userId: Long,
                                   learningPathId: Long): DBIO[Seq[UserMembership]] = {
    selectByUserIdAndLearningPathIdQ(userId, learningPathId).result
  }

  def getByUserIdsAndLearningPathId(userIds: Seq[Long],
                                    learningPathId: Long): DBIO[Seq[UserMembership]] = {
    userIds match {
      case Nil => DBIO.successful(Nil)
      case ids => usersMembershipTQ
        .filter(m => (m.userId inSet ids) && m.entityId === learningPathId)
        .result
    }
  }

  def getByUserIdAndLearningPathIds(userId: Long,
                                    learningPathIds: Seq[Long]): DBIO[Seq[UserMembership]] = {
    learningPathIds match {
      case Nil => DBIO.successful(Nil)
      case ids => usersMembershipTQ
        .filter(m => m.userId === userId && {m.entityId inSet ids})
        .result
    }
  }


  private val selectExistsByUserIdAndLearningPathIdQ = Compiled { (userId: Rep[Long],
                                                                   learningPathId: Rep[Long]) =>
    usersMembershipTQ
      .filter(m => m.userId === userId && m.entityId === learningPathId)
      .exists
  }

  def hasByUserIdAndLearningPathIdQ(userId: Long, learningPathId: Long): DBIO[Boolean] = {
    selectExistsByUserIdAndLearningPathIdQ(userId, learningPathId).result
  }


  private val selectByUserIdAndGroupIdQ = Compiled { (userId: Rep[Long],
                                                            groupId: Rep[Long]) =>
    usersMembershipTQ
      .filter(m => m.userId === userId && m.memberId === groupId)
  }

  def getByUserIdAndGroupId(userId: Long,
                            groupId: Long): DBIO[Seq[UserMembership]] = {
    selectByUserIdAndGroupIdQ(userId, groupId).result
  }
}
