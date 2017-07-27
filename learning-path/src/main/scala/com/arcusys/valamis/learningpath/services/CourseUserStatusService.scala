package com.arcusys.valamis.learningpath.services

import com.arcusys.valamis.learningpath.models.CourseUserStatus

import scala.concurrent.Future

/**
  * Created by pkornilov on 3/21/17.
  */
trait CourseUserStatusService {

  def getCourseStatusForUser(courseId: Long, userId: Long): Future[CourseUserStatus]

}