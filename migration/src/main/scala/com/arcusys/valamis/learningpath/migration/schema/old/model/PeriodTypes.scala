package com.arcusys.valamis.learningpath.migration.schema.old.model

import org.joda.time.{Period => JPeriod}

private[migration] case class Period(periodType: PeriodTypes.Value, value: Int)

private[migration] object Period {
  def unlimited: Period = Period(PeriodTypes.UNLIMITED, 0)
}

private[migration] object PeriodTypes extends Enumeration {
  type PeriodType = Value

  val UNLIMITED, YEAR, MONTH, WEEKS, DAYS = Value

  def parse(value: String) = value.toLowerCase() match {
    case "unlimited" => UNLIMITED
    case "year" => YEAR
    case "month" => MONTH
    case "weeks" => WEEKS
    case "days" => DAYS
    case _ => UNLIMITED
  }

  def apply(value: String) = parse(value)

  def apply(value: Option[String]) = value match {
    case Some(v) => parse(v)
    case None => UNLIMITED
  }

  def toJodaPeriod(periodType: PeriodType, value: Int): Option[JPeriod] = {
    periodType match {
      case PeriodTypes.DAYS      => Some(JPeriod.days(value))
      case PeriodTypes.WEEKS     => Some(JPeriod.weeks(value))
      case PeriodTypes.MONTH     => Some(JPeriod.months(value))
      case PeriodTypes.YEAR      => Some(JPeriod.years(value))
      case PeriodTypes.UNLIMITED => None
    }
  }


}
