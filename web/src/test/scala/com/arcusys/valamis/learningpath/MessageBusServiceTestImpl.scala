package com.arcusys.valamis.learningpath

import java.util

import com.arcusys.valamis.learningpath.models._
import com.arcusys.valamis.learningpath.services.MessageBusService
import com.arcusys.valamis.learningpath.services.exceptions.{AssignmentIsNotDeployedError, CoursesIsNotDeployedError, LessonsIsNotDeployedError, TrainingEventIsNotDeployedError}

import scala.util.{Failure, Success, Try}

/**
  * Created by pkornilov on 3/13/17.
  */
class MessageBusServiceTestImpl(assignmentsData: Map[Long, String],
                                assignmentStatusData: => Map[Long, Map[Long, UserStatuses.Value]],
                                isAssignmentDeployed: => Boolean,
                                lessonsData: Map[Long, String],
                                lessonStatusData: => Map[Long, Map[Long, Boolean]],
                                isLessonDeployed: => Boolean,
                                courseStatusData: => Map[Long, Map[Long, (Boolean, String)]],
                                isCourseDeployed: => Boolean
                               ) extends MessageBusService {


  private def getField(data: util.HashMap[String, AnyRef], name: String): Try[String] = Try {
    Option(data.get(name)) map (_.asInstanceOf[String]) getOrElse {
      throw new IllegalArgumentException(s" no $name field")
    }
  }

  override def sendSynchronousMessage(destinationName: String, data: util.HashMap[String, AnyRef]): Try[Object] = {
    destinationName match {
      case name if name.contains("assignment") => processAssignmentMessages(data)
      case name if name.contains("lesson") => processLessonMessages(data)
      case name if name.contains("course") => processCourseMessages(data)
    }
  }

  private def processCourseMessages(data: util.HashMap[String, AnyRef]): Try[Object] = {
    if (!isCourseDeployed) {
      Failure(new CoursesIsNotDeployedError())
    } else {
      Option(data.get(CourseMessageFields.Action)) match {

        case Some(action) =>
          CourseActionType.withName(action.asInstanceOf[String]) match {
            case CourseActionType.CourseStatus =>
              for {
                courseId <- getField(data, CourseMessageFields.CourseId)
                userId <- getField(data, CourseMessageFields.UserId)
              } yield {
                val userStatuses = courseStatusData.getOrElse(courseId.toLong,
                  throw new NoSuchElementException("no status data for course " + courseId))

                val (isCompleted, dateString) = userStatuses.getOrElse(userId.toLong,
                  throw new NoSuchElementException(s"no status data for course $courseId and user $userId")
                )

                s"""
                   |{
                   | "${CourseJsonFields.IsCompleted}": $isCompleted,
                   | "${CourseJsonFields.Date}": "$dateString"
                   |}
                """.stripMargin
              }
            case _ => Failure(new IllegalArgumentException("Unknown action: " + action))
          }
        case None => Failure(new IllegalArgumentException("no action"))
      }
    }
  }

  private def processLessonMessages(data: util.HashMap[String, AnyRef]): Try[Object] = {
    if (!isLessonDeployed) {
      Failure(new LessonsIsNotDeployedError())
    } else {
      Option(data.get(LessonMessageFields.Action)) match {

        case Some(action) =>
          LessonMessageActionType.withName(action.asInstanceOf[String]) match {

            case LessonMessageActionType.LessonNames =>
              getField(data, LessonMessageFields.LessonIds) map { idString =>
                val ids = idString.split(',').map(_.toLong)
                val items = ids flatMap { id =>
                  lessonsData.get(id).toSeq
                }
                s"""
                   |{
                   |  "${LessonJsonFields.Lessons}": [${items.mkString(",")}]
                   |}
                """.stripMargin
              }
            case LessonMessageActionType.LessonStatus =>
              for {
                lessonId <- getField(data, LessonMessageFields.LessonId)
                userId <- getField(data, LessonMessageFields.UserId)
              } yield {
                val status = lessonStatusData.get(lessonId.toLong).flatMap(_.get(userId.toLong)).contains(true)
                s"""{ "isCompleted": $status } """
              }
            case LessonMessageActionType.Check => Try("deployed")
            case _ => Failure(new IllegalArgumentException("Unknown action: " + action))
          }
        case None => Failure(new IllegalArgumentException("no action"))
      }
    }
  }

  private def processAssignmentMessages(data: util.HashMap[String, AnyRef]): Try[Object] = {
    if (!isAssignmentDeployed) {
      Failure(new AssignmentIsNotDeployedError())
    } else {
      Option(data.get(AssignmentMessageFields.Action)) match {

        case Some(action) =>
          AssignmentMessageActionType.withName(action.asInstanceOf[String]) match {

            case AssignmentMessageActionType.ByIds =>
              (Option(data.get(AssignmentMessageFields.AssignmentIds)).map(_.asInstanceOf[String]).filter(_.nonEmpty) match {
                case None =>
                  Failure(new IllegalArgumentException(s"no ${AssignmentMessageFields.AssignmentIds} field"))
                case Some(idString) => Success(idString.split(',').map(_.toLong))
              }) map { ids =>
                val items = ids flatMap { id =>
                  assignmentsData.get(id).toSeq
                }
                s"""
                   |{
                   |  "assignments": [${items.mkString(",")}]
                   |}
                """.stripMargin
              }

            case AssignmentMessageActionType.SubmissionStatus =>
              for {
                assignmentId <- getField(data, AssignmentMessageFields.AssignmentId)
                userId <- getField(data, AssignmentMessageFields.UserId)
              } yield {
                val userStatuses = assignmentStatusData.getOrElse(assignmentId.toLong,
                  throw new NoSuchElementException("no status data for assignment " + assignmentId))

                userStatuses.getOrElse(userId.toLong,
                  throw new NoSuchElementException(s"no status data for assignment $assignmentId and user $userId")
                ).toString
              }
            case AssignmentMessageActionType.Check => Try("deployed")
            case _ => Failure(new IllegalArgumentException("Unknown action: " + action))
          }
        case None => Failure(new IllegalArgumentException("no action"))
      }
    }
  }

  override def sendAsynchronousMessage(destinationName: String,
                                       data: util.HashMap[String, AnyRef]): Unit = {

  }
}
