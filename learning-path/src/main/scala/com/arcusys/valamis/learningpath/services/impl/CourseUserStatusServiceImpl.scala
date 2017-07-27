package com.arcusys.valamis.learningpath.services.impl

import com.arcusys.valamis.learningpath.models._
import com.arcusys.valamis.learningpath.services.exceptions.CoursesIsNotDeployedError
import com.arcusys.valamis.learningpath.services.{CourseUserStatusService, MessageBusService, MessageBusSupport}
import org.joda.time.DateTime
import org.json4s.ext.DateTimeSerializer
import org.json4s.{DefaultFormats, Formats}
import org.json4s.jackson.JsonMethods.parse

import scala.concurrent.Future

/**
  * Created by pkornilov on 3/21/17.
  */
class CourseUserStatusServiceImpl(val messageBusService: MessageBusService) extends CourseUserStatusService
  with MessageBusSupport {

  implicit val formats: Formats = DefaultFormats + DateTimeSerializer

  protected val destination = "valamis/courses"

  override protected def notDeployedError: Throwable = new CoursesIsNotDeployedError

  override def getCourseStatusForUser(courseId: Long,
                                      userId: Long): Future[CourseUserStatus] = {
    Future.fromTry {
      sendMessage(Map(
        CourseMessageFields.Action -> CourseActionType.CourseStatus.toString,
        CourseMessageFields.CourseId -> courseId.toString,
        CourseMessageFields.UserId -> userId.toString
      )) { json =>
        val value = parse(json)
        val isCompleted = (value \ CourseJsonFields.IsCompleted).extract[Boolean]
        val date = (value \ CourseJsonFields.Date).extract[DateTime]
        CourseUserStatus(isCompleted, date)
      }
    }
  }
}