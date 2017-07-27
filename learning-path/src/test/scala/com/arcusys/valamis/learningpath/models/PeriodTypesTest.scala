package com.arcusys.valamis.learningpath.models

import org.scalatest.FunSuite

/**
  * Created by mminin on 20/01/2017.
  */
class PeriodTypesTest extends FunSuite {

  test("ids should be like in previous versions") {
    assert(PeriodTypes.UNLIMITED.id == 0)
    assert(PeriodTypes.YEAR.id == 1)
    assert(PeriodTypes.MONTH.id == 2)
    assert(PeriodTypes.WEEKS.id == 3)
    assert(PeriodTypes.DAYS.id == 4)
  }

  test("names should be like in previous versions") {
    assert("unlimited" == PeriodTypes.UNLIMITED.toString)
    assert("year" == PeriodTypes.YEAR.toString)
    assert("month" == PeriodTypes.MONTH.toString)
    assert("weeks" == PeriodTypes.WEEKS.toString)
    assert("days" == PeriodTypes.DAYS.toString)
  }
}
