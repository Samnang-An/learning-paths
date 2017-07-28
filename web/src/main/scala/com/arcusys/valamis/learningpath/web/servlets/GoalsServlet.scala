package com.arcusys.valamis.learningpath.web.servlets

import com.arcusys.valamis.learningpath.models.{GoalTypes, GoalWebContent}
import com.arcusys.valamis.learningpath.services.exceptions.{NoGoalError, VersionPublishedError}
import com.arcusys.valamis.learningpath.services._
import com.arcusys.valamis.learningpath.web.servlets.base.LearningPathServletBase
import com.arcusys.valamis.learningpath.web.servlets.response._
import org.joda.time.Period
import org.scalatra.{MethodNotAllowed, NotFound}

import scala.concurrent.ExecutionContext


trait GoalsServlet {
  self: LearningPathServletBase =>

  implicit val executionContext: ExecutionContext

  protected val learningPathsPrefix: String
  protected val goalsPrefix: String

  protected val goalService: GoalService
  protected val goalGroupService: GoalsGroupService
  protected val goalLessonService: GoalLessonService
  protected val goalActivityService: GoalActivityService
  protected val goalAssignmentService: GoalAssignmentService
  protected val goalStatementService: GoalStatementService
  protected val goalWebContentService: GoalWebContentService
  protected val goalCourseService: GoalCourseService
  protected val goalTrainingEventService: GoalTrainingEventService

  private def id = params.getAsOrElse[Long]("id", halt(goalNotFound(params("id"))))

  private def goalNotFound(learningPathId: Any) = NotFound("no goal with id: " + learningPathId)

  implicit private val handleError: PartialFunction[Throwable, Nothing] = {
    case e: NoGoalError => halt(goalNotFound(e.goalId))
    case _: VersionPublishedError => halt(MethodNotAllowed(s"Learning path already published"))
  }

  put(s"$goalsPrefix/:id/?")(await {
    requireModifyPermission

    val timeLimit = (parsedBody \ "timeLimit").extract[Option[Period]]
    val optional = (parsedBody \ "optional").extractOrElse[Boolean](false)

    (parsedBody \ "goalType").extract[GoalTypes.Value] match {
      case GoalTypes.Lesson =>
        goalLessonService
          .update(id, timeLimit, optional)
          .map(GoalLessonResponse(_))

      case GoalTypes.LRActivity =>
        val count = (parsedBody \ "count").extractOrElse[Int](1)

        goalActivityService
          .update(id, timeLimit, optional, count)
          .map(GoalLRActivityResponse(_))

      case GoalTypes.Assignment =>
        goalAssignmentService
          .update(id, timeLimit, optional)
          .map(GoalAssignmentResponse(_))

      case GoalTypes.WebContent =>
        goalWebContentService
          .update(id, timeLimit, optional)
          .map(GoalWebContentResponse(_))

      case GoalTypes.Statement =>
        goalStatementService
          .update(id, timeLimit, optional)
          .map(GoalStatementResponse(_))

      case GoalTypes.Course =>
        goalCourseService
          .update(id, timeLimit, optional)
          .map(GoalCourseResponse(_))

      case GoalTypes.TrainingEvent =>
        goalTrainingEventService
          .update(
            id,
            (parsedBody \ "timeLimit").extract[Option[Period]],
            (parsedBody \ "optional").extractOrElse[Boolean](false)
          )
          .map(GoalTrainingEventResponse(_))
    }
  })

  delete(s"$goalsPrefix/:id/?")(await {
    requireModifyPermission

    goalService.delete(id)
  })

  post(s"$goalsPrefix/:id/move/?")(await {
    requireModifyPermission

    val newGroupId = (parsedBody \ "groupId").extractOpt[Long]
    val indexNumber = (parsedBody \ "indexNumber").extract[Int]

    goalService.move(id, newGroupId, indexNumber)
  })
}
