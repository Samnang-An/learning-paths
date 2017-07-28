package com.arcusys.valamis.learningpath.web.servlets.utils

import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatra.test.scalatest.ScalatraSuite

/**
  * Created by mminin on 24/01/2017.
  */
trait ResponseMatcher {
  _: ScalatraSuite =>
  def beOk = beCode(200)

  def beNoContent = beCode(204)

  def beNotFound = beCode(404)

  def beBadRequest = beCode(400)

  def beForbidden = beCode(403)

  def beNotAllowed = beCode(405)

  def beCode(code: Int) = Matcher { (status: Int) =>
    MatchResult(
      status == code,
      s"response status was not $code: $status $body",
      s"status was $status"
    )
  }
}
