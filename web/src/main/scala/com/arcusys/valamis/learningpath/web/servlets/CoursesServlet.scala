package com.arcusys.valamis.learningpath.web.servlets

import com.arcusys.valamis.learningpath.models.CourseSort
import com.arcusys.valamis.learningpath.services.CourseService
import com.arcusys.valamis.learningpath.web.servlets.base.LearningPathServletBase
import com.arcusys.valamis.learningpath.web.servlets.response.RangeResponse
import org.scalatra.NotFound

import scala.concurrent.ExecutionContext


trait CoursesServlet {
  self: LearningPathServletBase =>

  implicit val executionContext: ExecutionContext

  protected val coursesPrefix: String

  protected def courseService: CourseService

  get(s"$coursesPrefix/?")(await {
    val skip = params.getAs[Int]("skip").getOrElse(0)
    val take = params.getAs[Int]("take").getOrElse(10)
    val titleFilter = params.get("title")

    val sort = CourseSort.withName(params.getOrElse("sort", "title"))

    val itemsF = courseService.getCourses(titleFilter, sort, skip, take)
    val countF = courseService.getCoursesCount(titleFilter)

    for {
      items <- itemsF
      count <- countF
    } yield {
      RangeResponse(items, count)
    }
  })

  get(s"$coursesPrefix/:id/?")(await {
    val id = params.as[Long]("id")
    courseService.getCourseById(id)
      .map(_.getOrElse {
        halt(NotFound("no course with id: " + id))
      })
  })

}
