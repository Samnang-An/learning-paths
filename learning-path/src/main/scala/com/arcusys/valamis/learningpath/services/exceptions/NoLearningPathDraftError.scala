package com.arcusys.valamis.learningpath.services.exceptions

/**
  * Created by mminin on 07/03/2017.
  */
class NoLearningPathDraftError(val learningPathId: Long)
  extends Exception("no draft for learning path with id: " + learningPathId)
