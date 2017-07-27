package com.arcusys.valamis.learningpath.web.servlets

import com.arcusys.valamis.learningpath.models.WebContentSort
import com.arcusys.valamis.learningpath.services._
import com.arcusys.valamis.learningpath.web.servlets.base.LearningPathServletBase
import org.scalatra.NotFound

import scala.concurrent.ExecutionContext


trait WebContentServlet {
  self: LearningPathServletBase =>

  implicit val executionContext: ExecutionContext

  protected val webContentPrefix: String

  protected val webContentService: WebContentService

  get(s"$webContentPrefix/?") {
    val skip = params.getAs[Int]("skip").getOrElse(0)
    val take = params.getAs[Int]("take").getOrElse(10)
    val title = params.get("title")
    val sort = WebContentSort.withName(params.getOrElse("sort", "title"))
     webContentService.getAll(skip, take, sort, title)
  }

  get(s"$webContentPrefix/:id/?")( await {
    val webContentId = params.as[Long]("id")

    response.reset()
    response.contentType = Some("text/html")


    val result = webContentService.getContent(webContentId)
      .map(_.getOrElse {
        halt(NotFound())
      })
    webContentService.addCheckerTask(webContentId, currentUserId, companyId)

    result
  })
}
