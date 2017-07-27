package com.arcusys.valamis.learningpath.tasks

/**
  * custom exception to abort task without errors in the log
  *  ex: learning path already deleted
  */
class TaskAbortException(message: String) extends Exception(message)
