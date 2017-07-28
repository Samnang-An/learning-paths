package com.arcusys.valamis.learningpath.services.exceptions

/**
  * Created by mminin on 16/02/2017.
  */
class NoLearningPathError(val learningPathId: Long)
  extends Exception("no learning path with id: " + learningPathId)
