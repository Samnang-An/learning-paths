package com.arcusys.valamis.learningpath.services.exceptions

class NoVersionError(val versionId: Long)
  extends Exception("no version with id: " + versionId)
