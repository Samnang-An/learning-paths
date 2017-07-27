package com.arcusys.valamis.learningpath.models

object PeriodTypes extends Enumeration {
  type PeriodType = Value

  val UNLIMITED = Value("unlimited")
  val YEAR = Value("year")
  val MONTH = Value("month")
  val WEEKS = Value("weeks")
  val DAYS = Value("days")
}
