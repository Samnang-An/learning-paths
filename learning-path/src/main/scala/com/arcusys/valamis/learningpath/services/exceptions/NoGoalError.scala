package com.arcusys.valamis.learningpath.services.exceptions

/**
  * Created by mminin on 16/02/2017.
  */
class NoGoalError(val goalId: Long)
  extends Exception("no goal with id: " + goalId)
