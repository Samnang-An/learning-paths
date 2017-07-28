package com.arcusys.valamis.learningpath.services

import com.arcusys.valamis.learningpath.models.{Goal, GoalsSet}

import scala.concurrent.Future

/**
  * Created by mminin on 20/01/2017.
  */
trait GoalService {

  def get(id: Long)
         (implicit companyId: Long): Future[Option[Goal]]

  def getGoalsByLPCurrentVersion(learningPathId: Long)
                                (implicit companyId: Long): Future[GoalsSet]

  def getGoalsByVersion(versionId: Long)
                       (implicit companyId: Long): Future[GoalsSet]

  def getGoalsByLPDraftVersion(learningPathId: Long)
                              (implicit companyId: Long): Future[GoalsSet]

  def delete(goalId: Long)(implicit companyId: Long): Future[Unit]

  def move(goalId: Long, newGroupId: Option[Long], indexNumber: Int)
          (implicit companyId: Long): Future[Unit]
}
