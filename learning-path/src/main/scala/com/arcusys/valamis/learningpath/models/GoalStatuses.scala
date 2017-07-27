package com.arcusys.valamis.learningpath.models

object GoalStatuses extends Enumeration {
  val InProgress = Value("InProgress")
  val Failed = Value("Failed")
  val Success = Value("Success")
  val Undefined = Value("Undefined")
}