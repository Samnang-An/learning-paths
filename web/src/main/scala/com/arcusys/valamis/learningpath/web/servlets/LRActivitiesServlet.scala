package com.arcusys.valamis.learningpath.web.servlets

import com.arcusys.valamis.learningpath.services._
import com.arcusys.valamis.learningpath.web.servlets.base.LearningPathServletBase

import scala.concurrent.ExecutionContext


trait LRActivitiesServlet {
  self: LearningPathServletBase =>

  implicit val executionContext: ExecutionContext

  protected val activitiesPrefix: String

  protected def lrActivityTypeService: LRActivityTypeService

  get(s"$activitiesPrefix/?")(await {
    lrActivityTypeService.getAll
  })
}
