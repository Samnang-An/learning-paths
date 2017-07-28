package com.arcusys.valamis.learningpath.web.servlets

import com.arcusys.valamis.learningpath.services._
import com.arcusys.valamis.learningpath.services.exceptions.{NoLearningPathDraftError, NoLearningPathError}
import com.arcusys.valamis.learningpath.web.servlets.base.LearningPathServletBase
import org.scalatra.{BadRequest, NotFound}

import scala.concurrent.ExecutionContext

/**
  * Created by mminin on 23/01/2017.
  */
trait LearningPathsLogoServlet {
  self: LearningPathServletBase =>

  implicit val executionContext: ExecutionContext

  protected val learningPathsPrefix: String
  protected val logoFilesPrefix: String

  protected val logoService: LPLogoService
  protected def getMimeType(fileName: String): String

  implicit private val handleError: PartialFunction[Throwable, Nothing] = {
    case e: NoLearningPathError => halt(learningPathNotFound(e.learningPathId))
    case e: NoLearningPathDraftError => halt(learningPathNotFound(e.learningPathId)) //use different response message
  }

  private def learningPathNotFound(learningPathId: Any) =
    NotFound("no learning path with id: " + learningPathId)

  put(s"$learningPathsPrefix/:id/draft/logo/?")(await {
    requireModifyPermission

    val id = params.as[Long]("id")

    if (!request.contentType.exists(_.startsWith("image/"))) {
      halt(BadRequest("Unsupported content type: " + request.contentType.getOrElse("")))
    }

    //TODO: investigate and test
    //Content-Disposition:"attachment; filename="Vanamo_Logo.png""
    val name = request.header("Content-Disposition").map {
      disposition => disposition.split("\"")(1)
    } getOrElse {
      halt(BadRequest("no Content-Disposition"))
    }

    if (request.getContentLength <= 0) {
      halt(BadRequest("empty content"))
    }

    logoService.setDraftLogo(id, name, request.getInputStream).map { logoName =>
      val prefix = logoFilesPrefix.replaceFirst("/", "")
      Map("logoUrl" -> s"$prefix/$logoName")
    }
  })


  get(s"$logoFilesPrefix/:logoName/?") {
    val logoName = params("logoName")
    await(logoService.getLogo(logoName)) match {
      case None => halt(NotFound("file not found"))
      case Some(stream) =>
        response.reset()
        response.characterEncoding = None //without it 'charset' will be added to 'content type'
        response.setHeader("Content-Type", getMimeType(logoName))
        response.setHeader("Content-Disposition", s"""filename="$logoName"""")

        stream
    }
  }

  delete(s"$learningPathsPrefix/:id/draft/logo/?")(await {
    requireModifyPermission

    val id = params.as[Long]("id")

    logoService.deleteDraftLogo(id)
  })
}
