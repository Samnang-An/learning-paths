package com.arcusys.valamis.learningpath.web.servlets

import com.arcusys.valamis.learningpath.services._
import com.arcusys.valamis.learningpath.services.exceptions.NoVersionError
import com.arcusys.valamis.learningpath.web.servlets.base.LearningPathServletBase
import com.arcusys.valamis.learningpath.web.servlets.response._
import org.scalatra.NotFound

import scala.concurrent.ExecutionContext


trait VersionServlet {
  self: LearningPathServletBase =>

  implicit val executionContext: ExecutionContext

  protected val versionsPrefix: String

  protected val goalService: GoalService

  protected val recommendedCompetenceService: CompetenceService
  protected val improvingCompetenceService: CompetenceService

  private def id = params.getAsOrElse[Long]("id", halt(versionNotFound(params("id"))))

  private def versionNotFound(versionId: Any) =
    NotFound("no version with id: " + versionId)

  implicit private val handleError: PartialFunction[Throwable, Nothing] = {
    case e: NoVersionError => halt(versionNotFound(e.versionId))
  }

  get(versionsPrefix + "/:id/goals/tree/?")(await {
    goalService.getGoalsByVersion(id)
      .map(GoalsTreeBuilder.build)
  })

  get(versionsPrefix + "/:id/recommended-competences/?")(await {
    recommendedCompetenceService.getCompetencesByVersionId(id)
  })

  get(versionsPrefix + "/:id/improving-competences/?")(await {
    improvingCompetenceService.getCompetencesByVersionId(id)
  })
}
