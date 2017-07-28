package com.arcusys.valamis.learningpath.models

import org.joda.time.DateTime

case class Course(id: Long, title: String, friendlyUrl: String)

case class CourseUserStatus(isCompleted: Boolean,
                        date: DateTime)

object CourseSort extends Enumeration {
  val title = Value("title")
  val titleDesc = Value("-title")
}

object CourseActionType extends Enumeration {
  val CourseStatus = Value
  val Check = Value
}

object CourseJsonFields {
  val IsCompleted = "isCompleted"
  val Date = "date"
  val Id = "id"
  val Error = "error"
}

object CourseMessageFields {
  val Action = "action"
  val CourseId = "courseId"
  val UserId = "userId"
  val CompanyId = "companyId"
}
