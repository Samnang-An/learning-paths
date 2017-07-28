package com.arcusys.valamis.learningpath.serializer

import org.json4s.{DefaultFormats, Formats, JValue}
import org.json4s.JsonAST.{JInt, JString}
import org.json4s.ext.DateTimeSerializer

/**
  * Created by pkornilov on 3/15/17.
  */
trait SerializerBase {

  implicit val formats: Formats = DefaultFormats + DateTimeSerializer

  protected implicit class JValueLongExtractor(jv: JValue) {
    def extractLong(implicit formats: Formats, mf: scala.reflect.Manifest[Long]): Long = jv match {
      //under Liferay 7 JString is returned instead of JInt,
      //see com.liferay.portal.kernel.json.JSONObject.put(String key, long value)
      case v @ JString(_) => v.extract[String].toLong
      case v @ JInt(_) => v.extract[Long]
      case v => throw new IllegalArgumentException(v.toString)
    }
  }

}
