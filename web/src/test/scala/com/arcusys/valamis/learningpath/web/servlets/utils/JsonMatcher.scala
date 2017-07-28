package com.arcusys.valamis.learningpath.web.servlets.utils

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.scalatest.Matchers
import org.scalatest.matchers.{MatchResult, Matcher}


/**
  * Created by mminin on 24/01/2017.
  */
trait JsonMatcher {
  self: Matchers =>

  implicit def jsonFormat: Formats

  def haveJson(expected: String, ignoreValues: Seq[String] = Nil,
               strict: Boolean = false, sortBy: Option[String] = None) = new Matcher[String] {
    def apply(actual: String) = {

      // set same value to ignore fields then compare
      val expectedJson = parse(expected).replaceAll(ignoreValues, JNothing)

      val actualJson = parse(actual).replaceAll(ignoreValues, JNothing)

      val actualSorted = sortBy.fold(actualJson) { fieldName =>
        JArray(actualJson.extract[List[JValue]].sortWith {
          case (l, r) =>
            val left = (l \ fieldName).extract[Long]
            val right = (r \ fieldName).extract[Long]
            left < right
        })
      }

      check(expectedJson, actualSorted, strict)
    }
  }

  private def check(expected: JValue, actual: JValue, strict: Boolean) = {
    // we should ignore new fields in actual json
    // ex: expected is { id:2 } and actual is { id:2, creationDate:"..." } result is ok


    val diff = expected diff actual

    if (diff.changed != JNothing) {
      MatchResult(
        matches = false,
        s"expected json: ${compact(expected)} \n changed fields: ${compact(diff.changed)}",
        ""
      )
    } else if (diff.deleted != JNothing) {
      MatchResult(
        matches = false,
        "different json, not found fields: " + compact(diff.deleted),
        ""
      )
    } else if (strict && diff.added != JNothing) {
      //TODO refactor it so that added fields are ignored for objects, and checked for arrays
      MatchResult(
        matches = false,
        "different json, new fields: " + compact(diff.added),
        ""
      )
    } else {
      MatchResult(
        matches = true,
        "",
        "same json"
      )
    }
  }

  implicit class JValueExt(val jObject: JValue) {

    /**
      * find field by name and replace value, recursive
      */
    def replaceAll(keysToReplace: Seq[String], newValue: JValue): JValue = {
      jObject transform {
        case JObject(fields) => JObject(fields.map {
          case (key, _) if keysToReplace.contains(key) => (key, newValue)
          case (key, value) => (key, value.replaceAll(keysToReplace, newValue))
        })
        case JArray(items) => JArray(items.map { item =>
          item.replaceAll(keysToReplace, newValue)
        })
      }
    }

  }
}
