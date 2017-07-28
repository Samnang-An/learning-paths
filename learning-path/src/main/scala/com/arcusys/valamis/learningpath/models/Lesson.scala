package com.arcusys.valamis.learningpath.models

import org.joda.time.DateTime

/**
  * Created by pkornilov on 3/15/17.
  */
case class LessonStatus(
   isCompleted: Boolean,
   date: Option[DateTime]
)

object LessonJsonFields {
  val IsCompleted = "isCompleted"
  val Date = "date"
  val Lessons = "lessons"
  val Id = "id"
  val Title = "title"
}

object LessonMessageFields {
  val Action = "action"
  val LessonId = "lessonId"
  val LessonIds = "lessonIds"
  val UserId = "userId"
  val CompanyId = "companyId"
}

object LessonMessageActionType extends Enumeration {
  val LessonStatus = Value
  val LessonNames = Value
  val Check = Value
}
