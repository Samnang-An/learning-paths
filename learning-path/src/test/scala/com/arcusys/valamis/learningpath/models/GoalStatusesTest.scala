package com.arcusys.valamis.learningpath.models

import org.scalatest.FunSuite

/**
  * Created by mminin on 20/01/2017.
  */
class GoalStatusesTest extends FunSuite {
  test("ids should be like in previous versions") {
    assert(GoalStatuses.InProgress.id == 0)
    assert(GoalStatuses.Failed.id == 1)
    assert(GoalStatuses.Success.id == 2)
  }

  test("names should be like in previous versions") {
    assert("InProgress" == GoalStatuses.InProgress.toString)
    assert("Failed" == GoalStatuses.Failed.toString)
    assert("Success" == GoalStatuses.Success.toString)
  }
}
