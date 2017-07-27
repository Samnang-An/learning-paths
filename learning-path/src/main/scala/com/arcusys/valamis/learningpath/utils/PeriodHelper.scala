package com.arcusys.valamis.learningpath.utils

import com.arcusys.valamis.learningpath.models.PeriodTypes
import com.arcusys.valamis.learningpath.models.PeriodTypes.PeriodType
import org.joda.time.{DurationFieldType, Period}

import scala.util.{Failure, Success, Try}

object PeriodHelper {

  val emptyPeriod = (PeriodTypes.UNLIMITED, 0)

  def toValamisPeriod(period: Period): Try[(PeriodType, Int)] = {
    val pValues = period.getValues.zipWithIndex.collect {
      case (v, index) if v != 0 => (v, period.getFieldType(index))
    }
    if (pValues.length != 1) {
      Failure(new IllegalArgumentException("Only periods with exactly one field are supported"))
    } else {
      val (value, tpe) = pValues.head
      if (value < 0) {
        Failure(new IllegalArgumentException("Period value should be positive"))
      } else {
        val periodType = tpe match {
          case t if t == DurationFieldType.days() => PeriodTypes.DAYS
          case t if t == DurationFieldType.weeks() => PeriodTypes.WEEKS
          case t if t == DurationFieldType.months() => PeriodTypes.MONTH
          case t if t == DurationFieldType.years() => PeriodTypes.YEAR
          case t => return Failure(new IllegalArgumentException("Unsupported period type: " + t))
        }

        Success((periodType, value))
      }
    }
  }

}
