package com.arcusys.valamis.learningpath.web.servlets

import com.arcusys.valamis.learningpath.models._
import com.arcusys.valamis.learningpath.services._
import com.arcusys.valamis.learningpath.services.exceptions.{AlreadyActivatedError, NoLearningPathDraftError, NoLearningPathError}
import com.arcusys.valamis.learningpath.web.servlets.base.LearningPathServletBase
import com.arcusys.valamis.learningpath.web.servlets.response._
import org.joda.time.Period
import org.scalatra.{MethodNotAllowed, NotFound, BadRequest}

import scala.concurrent.ExecutionContext

/**
  * Created by mminin on 23/01/2017.
  */
trait LearningPathsServlet {
  self: LearningPathServletBase =>

  implicit val executionContext: ExecutionContext

  protected val learningPathsPrefix: String
  protected val logoFilesPrefix: String

  protected def learningPathService: LearningPathService

  protected val goalService: GoalService
  protected val goalGroupService: GoalsGroupService
  protected val goalLessonService: GoalLessonService
  protected val goalAssignmentService: GoalAssignmentService
  protected val goalActivityService: GoalActivityService
  protected val goalStatementService: GoalStatementService
  protected val goalWebContentService: GoalWebContentService
  protected val goalTrainingEventService: GoalTrainingEventService
  protected val goalCourseService: GoalCourseService
  protected val versionService: LPVersionService

  protected val recommendedCompetenceService: CompetenceService
  protected val improvingCompetenceService: CompetenceService

  protected val lessonService: LessonService

  protected def trainingEventServiceBridge: TrainingEventServiceBridge

  private def id = params.getAsOrElse[Long]("id", halt(learningPathNotFound(params("id"))))

  private def learningPathNotFound(learningPathId: Any) =
    NotFound("no learning path with id: " + learningPathId)

  implicit private val handleError: PartialFunction[Throwable, Nothing] = {
    case e: NoLearningPathError => halt(learningPathNotFound(e.learningPathId))
    case e: NoLearningPathDraftError => halt(
      NotFound(s"learning path with id: ${e.learningPathId} has no draft")
    )
    case e: AlreadyActivatedError => halt(
      MethodNotAllowed(e.getMessage)
    )
  }

  def skip = params.getAs[Int]("skip")

  def take = params.getAs[Int]("take").getOrElse(10)

  def filter = LearningPathFilter(
    params.getAs[String]("title"),
    params.getAs[Long]("courseId"),
    params.getAs[Long]("userId"),
    // without modify permission we returns only published and activated
    if (hasModifyPermission) params.getAs[Boolean]("published") else Some(true),
    if (hasModifyPermission) params.getAs[Boolean]("activated") else Some(true)
  )

  def sort = LearningPathSort.withName(params.getOrElse("sort", "title"))

  post(s"$learningPathsPrefix/?")(await {
    requireModifyPermission

    learningPathService.create(
      currentUserId,
      parsedBody.extract[LPProperties]
    ) map toResponse
  })

  get(s"$learningPathsPrefix/?")(await {
    val itemsF = learningPathService.getByFilter(filter, sort, skip, take)
    val countF = learningPathService.getCountByFilter(filter)

    for {
      items <- itemsF
      count <- countF
    } yield {
      Map("items" -> (items map toResponse), "total" -> count)
    }
  })

  get(s"$learningPathsPrefix/:id/?")(await {
    val learningPathId = id
    learningPathService.getById(learningPathId)
      .map(_.getOrElse {
        halt(learningPathNotFound(learningPathId))
      })
      .map(toResponse)
  })

  delete(s"$learningPathsPrefix/:id/?")(await {
    requireModifyPermission

    learningPathService.delete(id)
  })

  get(s"$learningPathsPrefix/:id/goals/tree/?")(await {
    goalService.getGoalsByLPCurrentVersion(id)
      .map(GoalsTreeBuilder.build)
  })


  post(s"$learningPathsPrefix/:id/draft/groups/?")(await {
    requireModifyPermission

    goalGroupService
      .create(
        id,
        (parsedBody \ "title").extract[String],
        (parsedBody \ "timeLimit").extract[Option[Period]],
        (parsedBody \ "optional").extractOrElse[Boolean](false),
        (parsedBody \ "count").extract[Option[Int]]
      )
      .map(GoalsGroupResponse(_))
  })

  get(s"$learningPathsPrefix/:id/goals/?")(await {
    ???
  })

  get(s"$learningPathsPrefix/:id/goals/export/?")(await {
    ???
  })

  post(s"$learningPathsPrefix/:id/clone/?")(await {
    requireModifyPermission

    learningPathService.clone(id)
      .map(toResponse)
  })

  post(s"$learningPathsPrefix/:id/deactivate/?")(await {
    requireModifyPermission

    learningPathService.deactivate(id)
  })

  post(s"$learningPathsPrefix/:id/activate/?")(await {
    requireModifyPermission

    learningPathService.activate(id)
  })


  get(s"$learningPathsPrefix/:id/draft/?")(await {
    val learningPathId = id
    learningPathService.getDraftById(learningPathId)
      .map(_.getOrElse {
        throw new NoLearningPathDraftError(learningPathId)
      })
      .map(toResponse)
  })

  post(s"$learningPathsPrefix/:id/draft/?")(await {
    requireModifyPermission

    versionService.createNewDraft(id).map(toResponse)
  })

  post(s"$learningPathsPrefix/:id/draft/publish/?")(await {
    requireModifyPermission

    learningPathService.publishDraft(id)
  })

  put(s"$learningPathsPrefix/:id/draft/?")(await {
    requireModifyPermission
          
    learningPathService.updateDraft(id, parsedBody.extract[LPProperties])
      .map(toResponse)
  })

  post(s"$learningPathsPrefix/:id/draft/goals/?")(await {
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
          .create(id, timeLimit, optional,lessonId)
          .map(GoalLessonResponse(_))

      case GoalTypes.LRActivity =>
        val activityName = (parsedBody \ "activityName").extract[String]
        val count = (parsedBody \ "count").extractOrElse[Int](1)

        goalActivityService
          .create(id, timeLimit, optional, activityName, count)
          .map(GoalLRActivityResponse(_))

      case GoalTypes.Assignment =>
        if (!goalAssignmentService.isAssignmentDeployed) {
          halt(BadRequest("Assignments portlet is not deployed"))
        }
        val assignmentId = (parsedBody \ "assignmentId").extract[Long]

        goalAssignmentService
          .create(id, timeLimit, optional, assignmentId)
          .map(GoalAssignmentResponse(_))

      case GoalTypes.WebContent =>
        val webContentId = (parsedBody \ "webContentId").extract[Long]

        goalWebContentService
          .create(id, timeLimit, optional, webContentId)
          .map(GoalWebContentResponse(_))

      case GoalTypes.TrainingEvent =>
        if (!trainingEventServiceBridge.isTrainingEventsDeployed(companyId)) {
          halt(BadRequest("Training events portlet is not deployed"))
        }
        val trainingEventId = (parsedBody \ "trainingEventId").extract[Long]
        goalTrainingEventService
          .create(id, timeLimit, optional, trainingEventId)
          .map(GoalTrainingEventResponse(_))

      case GoalTypes.Statement =>
        if (!lessonService.isValamisDeployed) {
          halt(BadRequest("Valamis is not deployed"))
        }
        val verbId = (parsedBody \ "verbId").extract[String]
        val objectId = (parsedBody \ "objectId").extract[String]
        val objectName = (parsedBody \ "objectName").extract[String]

        goalStatementService
          .create(id, timeLimit, optional, verbId, objectId, objectName)
          .map(GoalStatementResponse(_))

      case GoalTypes.Course =>
        val courseId = (parsedBody \ "courseId").extract[Long]

        goalCourseService
          .create(id, timeLimit, optional, courseId)
          .map(GoalCourseResponse(_))
    }
  })

  get(s"$learningPathsPrefix/:id/draft/goals/?")(await {
    ???
  })


  get(s"$learningPathsPrefix/:id/draft/goals/tree/?")(await {
    goalService.getGoalsByLPDraftVersion(id)
      .map(GoalsTreeBuilder.build)
  })

  //competences
  private def addCompetencesRoutes(service: => CompetenceService, path: String): Unit = {
    post(s"$learningPathsPrefix/:id/draft/$path/?")(await {
      requireModifyPermission

      val competence = parsedBody.extract[Competence]
      service.create(id, competence)
    })

    delete(s"$learningPathsPrefix/:id/draft/$path/skills/:skillId/?")(await {
      requireModifyPermission

      service.delete(id, skillId)
    })

    get(s"$learningPathsPrefix/:id/draft/$path/?")(await {
      service.getCompetencesForLPLastDraft(id)
    })

    get(s"$learningPathsPrefix/:id/$path/?")(await {
      service.getCompetencesForLPCurrentVersion(id)
    })
  }

  addCompetencesRoutes(recommendedCompetenceService, "recommended-competences")
  addCompetencesRoutes(improvingCompetenceService, "improving-competences")

  private def toResponse(data: (LearningPath, LPVersion)) = {
    LPResponse.apply(data._1, data._2, logoFilesPrefix.replaceFirst("/", ""))
  }

  private def toResponse(data: LPWithInfo) = {
    LPForMemberResponse.apply(data, logoFilesPrefix.replaceFirst("/", ""))
  }

  private def skillId = params.getAsOrElse[Long]("skillId", {
    halt(BadRequest("bad skillId"))
  })
}
