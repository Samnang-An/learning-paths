package com.arcusys.valamis.learningpath.web.servlets

import com.arcusys.valamis.learningpath.models.GoalTypes
import com.arcusys.valamis.learningpath.services.exceptions.{InvalidGoalsTreeError, NoGoalGroupError, VersionPublishedError}
import com.arcusys.valamis.learningpath.services._
import com.arcusys.valamis.learningpath.web.servlets.base.LearningPathServletBase
import com.arcusys.valamis.learningpath.web.servlets.response.{GoalWebContentResponse, _}
import org.joda.time.Period
import org.scalatra.{BadRequest, MethodNotAllowed, NotFound}

import scala.concurrent.ExecutionContext

trait GoalsGroupServlet {
  self: LearningPathServletBase =>

  implicit val executionContext: ExecutionContext

  protected val learningPathsPrefix: String
  protected val groupsPrefix: String

  protected val goalService: GoalService
  protected val goalGroupService: GoalsGroupService
  protected val goalLessonService: GoalLessonService
  protected val goalActivityService: GoalActivityService
  protected val goalAssignmentService: GoalAssignmentService
  protected val goalStatementService: GoalStatementService
  protected val goalTrainingEventService: GoalTrainingEventService
  protected val goalCourseService: GoalCourseService
  protected val goalWebContentService: GoalWebContentService

  protected val lessonService: LessonService

  protected def trainingEventServiceBridge: TrainingEventServiceBridge

  private def id = params.getAsOrElse[Long]("id", halt(goalNotFound(params("id"))))

  implicit private val handleError: PartialFunction[Throwable, Nothing] = {
    case e: NoGoalGroupError => halt(goalNotFound(e.groupId))
    case e: InvalidGoalsTreeError => halt(BadRequest(e.getMessage))
    case _: VersionPublishedError => halt(MethodNotAllowed(s"Learning path already published"))
  }

  get(s"$groupsPrefix/:id/?")(await {
    val groupId = id
    goalGroupService
      .get(groupId)
      .map(_.getOrElse(throw new NoGoalGroupError(groupId)))
      .map(GoalsGroupResponse(_))
  })

  put(s"$groupsPrefix/:id/?")(await {
    requireModifyPermission

    goalGroupService
      .update(
        id,
        (parsedBody \ "title").extract[String],
        (parsedBody \ "timeLimit").extract[Option[Period]],
        (parsedBody \ "optional").extractOrElse[Boolean](false),
        (parsedBody \ "count").extract[Option[Int]]
      )
      .map(GoalsGroupResponse(_))
  })

  delete(s"$groupsPrefix/:id/?")(await {
    requireModifyPermission

    goalGroupService.delete(id)
  })

  post(s"$groupsPrefix/:id/groups/?")(await {
    requireModifyPermission

    goalGroupService
      .createInGroup(
        id,
        (parsedBody \ "title").extract[String],
        (parsedBody \ "timeLimit").extract[Option[Period]],
        (parsedBody \ "optional").extractOrElse[Boolean](false),
        (parsedBody \ "count").extract[Option[Int]]
      )
      .map(GoalsGroupResponse(_))
  })

  post(s"$groupsPrefix/:id/goals/?")(await {
    requireModifyPermission

    val timeLimit = (parsedBody \ "timeLimit").extract[Option[Period]]
    val optional = (parsedBody \ "optional").extractOrElse[Boolean](false)

    (parsedBody \ "goalType").extract[GoalTypes.Value] match {
      case GoalTypes.Lesson =>
        if (!lessonService.isValamisDeployed) {
          halt(BadRequest("Valamis is not deployed"))
        }
        val lessonId = (parsedBody \ "lessonId").extract[Long]

        goalLessonService
          .createInGroup(id, timeLimit, optional,lessonId)
          .map(GoalLessonResponse(_))

      case GoalTypes.LRActivity =>
        val activityName = (parsedBody \ "activityName").extract[String]
        val count = (parsedBody \ "count").extractOrElse[Int](1)

        goalActivityService
          .createInGroup(id, timeLimit, optional, activityName, count)
          .map(GoalLRActivityResponse(_))

      case GoalTypes.Assignment =>
        if (!goalAssignmentService.isAssignmentDeployed) {
          halt(BadRequest("Assignments portlet is not deployed"))
        }
        val assignmentId = (parsedBody \ "assignmentId").extract[Long]

        goalAssignmentService
          .createInGroup(id, timeLimit, optional, assignmentId)
          .map(GoalAssignmentResponse(_))

      case GoalTypes.WebContent =>
        val webContentId = (parsedBody \ "webContentId").extract[Long]

        goalWebContentService
          .createInGroup(id, timeLimit, optional, webContentId)
          .map(GoalWebContentResponse(_))

      case GoalTypes.TrainingEvent =>
        if (!trainingEventServiceBridge.isTrainingEventsDeployed(companyId)) {
          halt(BadRequest("Training events portlet is not deployed"))
        }
        val trainingEventId = (parsedBody \ "trainingEventId").extract[Long]
        goalTrainingEventService
          .createInGroup(id, timeLimit, optional, trainingEventId)
          .map(GoalTrainingEventResponse(_))

      case GoalTypes.Course =>
        val courseId = (parsedBody \ "courseId").extract[Long]
        goalCourseService
          .createInGroup(id, timeLimit, optional, courseId)
          .map(GoalCourseResponse(_))

      case GoalTypes.Statement =>
        if (!lessonService.isValamisDeployed) {
          halt(BadRequest("Valamis is not deployed"))
        }
        val verbId = (parsedBody \ "verbId").extract[String]
        val objectId = (parsedBody \ "objectId").extract[String]
        val objectName = (parsedBody \ "objectName").extract[String]

        goalStatementService
          .createInGroup(id, timeLimit, optional, verbId, objectId, objectName)
          .map(GoalStatementResponse(_))
    }
  })

  post(s"$groupsPrefix/:id/move/?")(await {
    requireModifyPermission

    val newGroupId = (parsedBody \ "groupId").extractOpt[Long]
    val indexNumber = (parsedBody \ "indexNumber").extract[Int]

    goalService.move(id, newGroupId, indexNumber)
  })

  private def goalNotFound(learningPathId: Any) = {
    NotFound("no goal with id: " + learningPathId)
  }
}
