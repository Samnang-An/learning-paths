package com.arcusys.valamis.learningpath.services.exceptions


class AlreadyActivatedError(val learningPathId: Long)
  extends Exception("learning path with id: " + learningPathId + " already activated")
