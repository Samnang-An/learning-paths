package com.arcusys.valamis.learningpath.services.impl

import com.arcusys.valamis.learningpath.models.{Goal, GoalGroup, GoalTypes}
import com.arcusys.valamis.learningpath.services.GoalsGroupService
import com.arcusys.valamis.learningpath.services.exceptions.{NoGoalGroupError, VersionPublishedError}
import com.arcusys.valamis.learningpath.utils.DbActions
import org.joda.time.{DateTime, Period}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

/**
  * Created by mminin on 26/01/2017.
  */
class GoalsGroupServiceImpl(val dbActions: DbActions)
                           (implicit val executionContext: ExecutionContext)
  extends GoalsGroupService
    with GoalServiceBaseImpl {

  import profile.api._

  override val goalType = GoalTypes.Group

  override def noCustomGoalEx(id: Long) = new NoGoalGroupError(id)

  def get(id: Long)
         (implicit companyId: Long): Future[Option[(Goal, GoalGroup)]] = {
    //TODO: validate companyId

    db.run(goalGroupDBIO.getWithGoalInfo(id))
  }

  override def create(learningPathId: Long,
                      title: String,
                      timeLimit: Option[Period],
                      optional: Boolean,
                      count: Option[Int])
                     (implicit companyId: Long): Future[(Goal, GoalGroup)] = db.run {
    createGoalAction(learningPathId, timeLimit, optional) { goalId =>
      goalGroupDBIO.insert(GoalGroup(goalId, title, count))
    } map { goal =>
      (goal, GoalGroup(goal.id, title, count))
    } transactionally
  }

  override def createInGroup(parentGroupId: Long,
                             title: String,
                             timeLimit: Option[Period],
                             optional: Boolean,
                             count: Option[Int])
                            (implicit companyId: Long): Future[(Goal, GoalGroup)] = db.run {
    createInGroupAction(parentGroupId, timeLimit, optional) { goalId =>
      goalGroupDBIO.insert(GoalGroup(goalId, title, count))
    } map { goal =>
      (goal, GoalGroup(goal.id, title, count))
    } transactionally
  }

  override def update(groupId: Long,
                      title: String,
                      timeLimit: Option[Period],
                      optional: Boolean,
                      count: Option[Int])
                     (implicit companyId: Long): Future[(Goal, GoalGroup)] = db.run {
    updateAction(groupId, timeLimit, optional)(goalGroupDBIO.get(groupId)) {
      goalGroupDBIO.update(groupId, title, count)
    } map { case (goal, _) =>
      (goal, GoalGroup(groupId, title, count))
    } transactionally
  }

  def delete(groupId: Long)
            (implicit companyId: Long): Future[Unit] = {
    val now = DateTime.now()

    val action = for {
      versionId <- goalGroupDBIO.getVersionId(groupId) map {
        _.getOrElse(throw new NoGoalGroupError(groupId))
      }
      _ <- versionDBIO.isPublishedById(versionId).map(_.collect {
        case true => throw new VersionPublishedError
      })
      _ <- deleteGroup(groupId)
      _ <- versionDBIO.updateModifiedDate(versionId, now)
    } yield {}

    db.run(action.transactionally)
  }

  private def deleteGroup(groupId: Long): DBIO[Unit] = {
    for {
      subGroups <- goalDBIO.getByGroupIdAndType(groupId, GoalTypes.Group)
      _ <- DBIO.seq(subGroups.map(g => deleteGroup(g.id)): _*)

      _ <- goalDBIO.deleteByGroupId(groupId)
      _ <- goalDBIO.delete(groupId)
    } yield {}
  }
}
