package com.arcusys.valamis.learningpath.services.impl

import com.arcusys.valamis.learningpath.models.{Goal, GoalTypes}
import com.arcusys.valamis.learningpath.services.exceptions.{NoGoalError, NoGoalGroupError, NoLearningPathDraftError, VersionPublishedError}
import com.arcusys.valamis.learningpath.utils.DbActionsSupport
import org.joda.time.{DateTime, Period}
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

/**
  * Created by mminin on 21/02/2017.
  */
trait GoalServiceBaseImpl extends DbActionsSupport {

  def goalType: GoalTypes.Value

  def noCustomGoalEx(id: Long): Exception = new NoGoalError(id)

  implicit def executionContext: ExecutionContext

  protected def nextIndexNumber(versionId: Long): DBIO[Int] = {
    goalDBIO.getCountByVersionId(versionId)
  }

  protected def nextIndexNumberInGroup(groupId: Long): DBIO[Int] = {
    goalDBIO.getCountByGroupId(groupId)
  }

  def createGoalAction(learningPathId: Long,
                       timeLimit: Option[Period],
                       optional: Boolean)(customAction: Long => DBIO[Int])
                      (implicit companyId: Long): DBIO[Goal] = {
    val now = DateTime.now()

    for {
    /*the method versionDBIO.updateModifiedDate must be called prior
    to calling the method goalDBIO.insert to avoid deadlock on mysql database*/
      (versionId, _) <- versionDBIO.getDraftByLearningPathId(learningPathId)
        .map(_.getOrElse(throw new NoLearningPathDraftError(learningPathId)))
      _ <- versionDBIO.updateModifiedDate(versionId, now)
      indexNumber <- nextIndexNumber(versionId)
      goalId <- {
        goalDBIO.insert(None, versionId, None, goalType, indexNumber, timeLimit, optional, now)
      }
      _ <- customAction(goalId)
      goal <- goalDBIO.get(goalId)
    } yield {
      goal.get
    }

  }

  def createInGroupAction(parentGroupId: Long,
                          timeLimit: Option[Period],
                          optional: Boolean)(customAction: Long => DBIO[Int])
                         (implicit companyId: Long): DBIO[Goal] = {
    val now = DateTime.now()

    for {
      versionId <- goalGroupDBIO.getVersionId(parentGroupId) map {
        _.getOrElse(throw new NoGoalGroupError(parentGroupId))
      }
      _ <- versionDBIO.isPublishedById(versionId).map(_.collect {
        case true => throw new VersionPublishedError
      })
      indexNumber <- nextIndexNumberInGroup(parentGroupId)
      goalId <- {
        goalDBIO.insert(None, versionId, Some(parentGroupId), goalType, indexNumber, timeLimit, optional, now)
      }
      _ <- customAction(goalId)
      _ <- versionDBIO.updateModifiedDate(versionId, now)
      goal <- goalDBIO.get(goalId)
    } yield {
      goal.get
    }

  }

  def updateAction[T](goalId: Long,
                      timeLimit: Option[Period],
                      optional: Boolean)(customGet: DBIO[Option[T]])(customUpdate: DBIO[Int] = DBIO.successful(0))
                     (implicit companyId: Long): DBIO[(Goal, T)] = {
    val now = DateTime.now()

    for {
      goalCustom <- customGet map {
        _.getOrElse(throw noCustomGoalEx(goalId))
      }
      goal <- goalDBIO.get(goalId) map {
        _.getOrElse(throw new NoGoalError(goalId))
      }
      _ <- versionDBIO.isPublishedById(goal.versionId).map(_.collect {
        case true => throw new VersionPublishedError
      })
      _ <- goalDBIO.update(goalId, timeLimit, optional, now)
      _ <- customUpdate
      _ <- versionDBIO.updateModifiedDate(goal.versionId, now)
      updatedGoal <- goalDBIO.get(goalId).map(_.get)
    } yield {
      (updatedGoal, goalCustom)
    }

  }
}
