package com.arcusys.valamis.learningpath.services.exceptions

/**
  * Created by mminin on 16/02/2017.
  */
class NoGoalGroupError(val groupId: Long)
  extends Exception("no goal group with id: " + groupId)
