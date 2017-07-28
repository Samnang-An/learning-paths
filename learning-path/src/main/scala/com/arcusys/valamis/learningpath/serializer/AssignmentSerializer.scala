package com.arcusys.valamis.learningpath.serializer

import com.arcusys.valamis.learningpath.models.{Assignment, AssignmentJsonFields}
import org.joda.time.format.ISODateTimeFormat
import org.json4s._
import org.json4s.jackson.JsonMethods.parse

import scala.language.implicitConversions

trait AssignmentSerializer extends SerializerBase {

  def deserializeAssignment(json: String): Assignment = extractAssignmentFields(parse(json))

  def deserializeAssignments(json: String): Seq[Assignment] = {
    val jsonValue = parse(json)
    for {
      assignment <- (jsonValue \ AssignmentJsonFields.Assignments).children
    } yield extractAssignmentFields(assignment)
  }

  private def extractAssignmentFields(jValue: JValue): Assignment = {
    val id = (jValue \ AssignmentJsonFields.Id).extractLong
    val title = (jValue \ AssignmentJsonFields.Title).extract[String]
    val body = (jValue \ AssignmentJsonFields.Body).extract[String]
    val deadlineString = jValue \ AssignmentJsonFields.Deadline match {
      case JNothing | JNull => None
      case value: JValue => Option(value.extract[String])
      case _ => throw new IllegalArgumentException(AssignmentJsonFields.Deadline)
    }
    val deadline = deadlineString.map(ISODateTimeFormat.dateTimeNoMillis().parseDateTime)

    Assignment(id,
      title,
      body,
      deadline)
  }

}