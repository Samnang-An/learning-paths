package com.arcusys.valamis.learningpath.utils

import org.json4s.ext.DateTimeSerializer
import org.json4s.jackson.JsonMethods
import org.json4s.{DefaultFormats, Extraction, Formats, JValue}

/**
  * Created by pkornilov on 1/16/17.
  */
object JsonHelper extends JsonMethods {

  implicit val formats: Formats = DefaultFormats + DateTimeSerializer

  def toJson[T](obj: T)(implicit formats: Formats): String =
    compact(render(Extraction.decompose(obj)))

  def fromJson(obj: String): JValue =
    parse(obj)

}
