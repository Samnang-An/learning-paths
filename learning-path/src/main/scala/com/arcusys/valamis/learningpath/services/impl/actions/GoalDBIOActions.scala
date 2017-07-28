package com.arcusys.valamis.learningpath.services.impl.actions

import com.arcusys.valamis.learningpath.models.{Goal, GoalTypes}
import com.arcusys.valamis.learningpath.services.impl.tables.LearningPathTables
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickProfile
import org.joda.time.{DateTime, Period}
import slick.driver.JdbcProfile

import scala.language.postfixOps

/**
  * Created by mminin on 26/01/2017.
  */
class GoalDBIOActions(val profile: JdbcProfile)
  extends LearningPathTables
    with SlickProfile {

  import profile.api._


  private val selectByIdQ = Compiled { id: Rep[Long] =>
    goalTQ.filter(_.id === id)
  }

  private val selectPropertiesByIdQ = Compiled { id: Rep[Long] =>
    goalTQ
      .filter(_.id === id)
      .map(g => (g.timeLimit, g.optional, g.modifiedDate))
  }

  private val selectIndexNumberByIdQ = Compiled { id: Rep[Long] =>
    goalTQ.filter(_.id === id).map(_.indexNumber)
  }

  private val selectGroupAndIndexNumberByIdQ = Compiled { id: Rep[Long] =>
    goalTQ
      .filter(_.id === id)
      .map(g => (g.groupId, g.indexNumber))
  }

  private val selectByVersionIdQ = Compiled { versionId: Rep[Long] =>
    goalTQ.filter(g => g.versionId === versionId)
  }

  private val selectCountByVersionIdQ = Compiled { versionId: Rep[Long] =>
    goalTQ
      .filter(g => g.versionId === versionId && g.groupId.isEmpty)
      .length
  }

  private val selectByVersionIdAndEmptyGroupIdQ = Compiled { versionId: Rep[Long] =>
    goalTQ.filter(g => g.versionId === versionId && g.groupId.isEmpty)
  }

  private val selectByVersionIdTypeQ = Compiled { (versionId: Rep[Long],
                                                   goalType: Rep[GoalTypes.Value]) =>
    goalTQ
      .filter(g => g.versionId === versionId && g.goalType === goalType)
  }

  private val selectByVersionAndGroupIdQ = Compiled { (versionId: Rep[Long], groupId: Rep[Long]) =>
    goalTQ.filter(g => g.versionId === versionId && g.groupId === groupId)
  }

  private val selectByGroupIdQ = Compiled { (groupId: Rep[Long]) =>
    goalTQ.filter(g => g.groupId === groupId)
  }

  private val selectCountByGroupIdQ = Compiled { groupId: Rep[Long] =>
    goalTQ.filter(g => g.groupId === groupId).length
  }

  private val selectByGroupIdAndTypeQ = Compiled { (groupId: Rep[Long],
                                                    goalType: Rep[GoalTypes.Value]) =>

    goalTQ.filter(g => g.goalType === goalType && g.groupId === groupId)
  }

  private val selectDataFieldsFieldsQ = Compiled {
    goalTQ.map(e => (e.oldGoalId, e.versionId, e.groupId, e.goalType,
      e.indexNumber, e.timeLimit, e.optional, e.modifiedDate))
  }

  def insert(oldGoalId: Option[Long],
             versionId: Long,
             groupId: Option[Long],
             goalType: GoalTypes.Value,
             indexNumber: Int,
             timeLimit: Option[Period],
             optional: Boolean,
             now: DateTime): DBIO[Long] = {
    selectDataFieldsFieldsQ returning goalTQ.map(_.id) +=
      (oldGoalId, versionId, groupId, goalType, indexNumber, timeLimit, optional, now)
  }

  def update(id: Long,
             timeLimit: Option[Period],
             optional: Boolean,
             modifiedDate: DateTime) : DBIO[Int] = {
    selectPropertiesByIdQ(id) update (timeLimit, optional, modifiedDate)
  }

  def updateIndexNumber(id: Long,
                        newIndexNumber: Int): DBIO[Int] = {
    selectIndexNumberByIdQ(id) update newIndexNumber
  }

  def updateGroupAndIndexNumber(id: Long,
                                newGroupId: Option[Long],
                                newIndexNumber: Int): DBIO[Int] = {
    selectGroupAndIndexNumberByIdQ(id) update (newGroupId, newIndexNumber)
  }

  def delete(id: Long) : DBIO[Int] = {
    selectPropertiesByIdQ(id).delete
  }

  def deleteByGroupId(groupId: Long): DBIO[Int] = {
    selectByGroupIdQ(groupId).delete
  }

  def get(id: Long): DBIO[Option[Goal]] = {
    selectByIdQ(id).result.headOption
  }

  def getByVersionId(versionId: Long): DBIO[Seq[Goal]] = {
    selectByVersionIdQ(versionId).result
  }

  def getByVersionIdAndType(versionId: Long,
                            goalType: GoalTypes.Value): DBIO[Seq[Goal]] = {
    selectByVersionIdTypeQ(versionId, goalType) result
  }

  def getByVersionIdAndParentGroupId(versionId: Long,
                                     parentGroupId: Option[Long]): DBIO[Seq[Goal]] = {
    parentGroupId map { groupId =>
      selectByVersionAndGroupIdQ(versionId, groupId)
    } getOrElse {
      selectByVersionIdAndEmptyGroupIdQ(versionId)
    } result
  }


  private val selectGroupIdByLearningPathIdQ = Compiled { learningPathId: Rep[Long] =>
    goalTQ
      .filter(_.versionId in versionTQ.filter(_.learningPathId === learningPathId).map(_.id))
      .map(_.groupId)
  }

  private val selectByLearningPathIdQ = Compiled { learningPathId: Rep[Long] =>
    goalTQ
      .filter(_.versionId in versionTQ.filter(_.learningPathId === learningPathId).map(_.id))
  }

  def deleteByLearningPathId(learningPathId: Long): DBIO[Int] = {
    { // mysql fails if table linked to self (MySQLIntegrityConstraintViolationException)
      selectGroupIdByLearningPathIdQ(learningPathId) update None
    } andThen {
      selectByLearningPathIdQ(learningPathId).delete
    }
  }


  def getCountByVersionId(versionId: Long): DBIO[Int] = {
    selectCountByVersionIdQ(versionId).result
  }

  def getCountByGroupId(groupId: Long): DBIO[Int] = {
    selectCountByGroupIdQ(groupId).result
  }

  def getByGroupIdAndType(groupId: Long, goalType: GoalTypes.Value): DBIO[Seq[Goal]] = {
    selectByGroupIdAndTypeQ(groupId, goalType).result
  }
}
