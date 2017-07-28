package com.arcusys.valamis.learningpath.services

import scala.util.{Failure, Try}

object MessageBusDestinations {

  val AssignmentCompleted = "valamis/main/assignmentCompleted"
  val LessonCompleted = "valamis/lessons/completed"
  val CourseCompleted = "valamis/courses/completed"
  val TrainingEventCompleted = "valamis/trainingEvents/completed"
  val LearningPathEndPoint = "valamis/learningPath"
  val CompetenceLevelChangedOrDeleted = "valamis/competences/levels/changedOrDeleted"
  val CompetenceSkillChangedOrDeleted = "valamis/competences/skills/changedOrDeleted"
  val ImproveCompetencesForUser = "valamis/competences/improve"

}

/**
  * Created by pkornilov on 3/15/17.
  */
trait MessageBusSupport {

  protected def messageBusService: MessageBusService

  protected def destination: String

  protected def notDeployedError: Throwable


  protected def sendMessage[T](data: Map[String, AnyRef])(convert: (String) => T): Try[T] = {
    handleMessageResponse[T](
      messageBusService.sendSynchronousMessage(destination, prepareMessageData(data)), convert
    )
  }

  protected def prepareMessageData(data: Map[String, AnyRef]): java.util.HashMap[String, AnyRef] = {
    val messageValues = new java.util.HashMap[String, AnyRef]()
    data.keys.map(k => messageValues.put(k, data(k)))

    messageValues
  }

  protected def handleMessageResponse[T](response: Try[Object], convert: (String) => T): Try[T] = {
    response.flatMap {
      case null => Failure(notDeployedError)
      case json: String if json.nonEmpty => Try(convert(json))
      case value => Failure(new Exception(s"Unsupported response: $value"))
    }
  }
}
