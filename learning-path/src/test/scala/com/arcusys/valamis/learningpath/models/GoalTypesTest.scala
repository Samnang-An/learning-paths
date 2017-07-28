package com.arcusys.valamis.learningpath.models

import org.scalatest.FunSuite

class GoalTypesTest extends FunSuite {
  test("ids should be like stored in db") {
    assert(GoalTypes.Group.id == 0)
    assert(GoalTypes.Lesson.id == 1)
    assert(GoalTypes.LRActivity.id == 2)
    assert(GoalTypes.Assignment.id == 3)
    assert(GoalTypes.WebContent.id == 4)
    assert(GoalTypes.TrainingEvent.id == 5)
    assert(GoalTypes.Statement.id == 6)
    assert(GoalTypes.Course.id == 7)
  }
}
