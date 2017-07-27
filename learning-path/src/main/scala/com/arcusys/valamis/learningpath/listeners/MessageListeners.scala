package com.arcusys.valamis.learningpath.listeners

import com.arcusys.valamis.learningpath.listeners.competences.{LevelChangeListener, SkillChangeListener}
import com.arcusys.valamis.learningpath.services.MessageBusDestinations
import com.arcusys.valamis.learningpath.services.impl.CompetenceServiceImpl
import com.arcusys.valamis.learningpath.utils.DbActions

import scala.concurrent.ExecutionContext

/**
  * Created by pkornilov on 6/20/17.
  */
object MessageListeners {

  def competencesLevelChangeListener(dbActions: DbActions)
                                    (implicit executionContext: ExecutionContext) = new LevelChangeListener(
    new CompetenceServiceImpl(dbActions, dbActions.recommendedCompetenceDBIOActions),
    new CompetenceServiceImpl(dbActions, dbActions.improvingCompetenceDBIOActions)
  )

  def competencesSkillChangeListener(dbActions: DbActions)
                                    (implicit executionContext: ExecutionContext) = new SkillChangeListener(
    new CompetenceServiceImpl(dbActions, dbActions.recommendedCompetenceDBIOActions),
    new CompetenceServiceImpl(dbActions, dbActions.improvingCompetenceDBIOActions)
  )

  def list(dbActions: DbActions)
          (implicit executionContext: ExecutionContext) = Map(
    MessageBusDestinations.CompetenceLevelChangedOrDeleted -> competencesLevelChangeListener(dbActions),
    MessageBusDestinations.CompetenceSkillChangedOrDeleted -> competencesSkillChangeListener(dbActions)
  )
}
