package com.arcusys.valamis.learningpath.serializer

import com.arcusys.valamis.learningpath.models.LessonJsonFields
import org.json4s._
import org.json4s.jackson.JsonMethods.parse

import scala.language.implicitConversions

trait LessonSerializer extends SerializerBase {

  def deserializeLessonNames(json: String): Map[Long, String] = {
    val jsonValue = parse(json)
    (for {
      lesson <- (jsonValue \ LessonJsonFields.Lessons).children
    } yield extractLessonFields(lesson)).toMap
  }

  private def extractLessonFields(jValue: JValue): (Long, String) = {
    val id = (jValue \ LessonJsonFields.Id).extractLong
    val title = (jValue \ LessonJsonFields.Title).extract[String]
    (id, title)
  }

}