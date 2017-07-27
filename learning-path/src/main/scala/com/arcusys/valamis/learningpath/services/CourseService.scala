package com.arcusys.valamis.learningpath.services

import com.arcusys.valamis.learningpath.models.{Course, CourseSort}

import scala.concurrent.Future

/**
  * Created by mminin on 28/02/2017.
  */
trait CourseService {

  def getCourses(titleFilter: Option[String],
                 sort: CourseSort.Value,
                 skip: Int,
                 take: Int)
                (implicit companyId: Long): Future[Seq[Course]]

  def getCoursesCount(titleFilter: Option[String])
                     (implicit companyId: Long): Future[Int]

  def getCourseTitlesByIds(ids: Seq[Long]): Future[Map[Long, String]]

  def getCourseById(id: Long): Future[Option[Course]]
}
