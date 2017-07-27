package com.arcusys.valamis.learningpath.services.impl.utils

import com.arcusys.valamis.learningpath.models.CompetenceDbEntity
import com.arcusys.valamis.learningpath.services.impl.actions.{ImprovingCompetenceDBIOActions, RecommendedCompetenceDBIOActions}
import com.arcusys.valamis.learningpath.utils.DbActionsSupport

import scala.concurrent.ExecutionContext

/**
  * Created by pkornilov on 6/14/17.
  */
trait CopyCompetencesSupport { self: DbActionsSupport =>

  protected implicit val executionContext: ExecutionContext

  def improvingCompetenceDBIO: ImprovingCompetenceDBIOActions
  def recommendedCompetenceDBIO: RecommendedCompetenceDBIOActions

  import profile.api._

  protected def copyCompetences(sourceVersionId: Long, newVersionId: Long): DBIO[Unit] = {
    for {
      improvingCompetences <- improvingCompetenceDBIO.getByVersionId(sourceVersionId)
      _ <- DBIO.sequence(improvingCompetences map { c =>
        improvingCompetenceDBIO.insert(new CompetenceDbEntity(newVersionId, c))
      })

      recommendedCompetences <- recommendedCompetenceDBIO.getByVersionId(sourceVersionId)
      _ <- DBIO.sequence(recommendedCompetences map { c =>
        recommendedCompetenceDBIO.insert(new CompetenceDbEntity(newVersionId, c))
      })
    } yield ()
  }


}
