package com.arcusys.valamis.learningpath.models

import org.joda.time.DateTime

case class Assignment(id: Long,
                      title: String,
                      body: String,
                      deadline: Option[DateTime])
//other assignment fields will be added when they are needed

object UserStatuses extends Enumeration {
  val WaitingForSubmission = Value
  val WaitingForEvaluation = Value
  val Completed = Value
}

//TODO remove redundant fields
object AssignmentJsonFields {
  val Assignments = "assignments"
  val Body = "body"
  val CompletedCount = "completedCount"
  val Count = "count"
  val Course = "course"
  val Date = "date"
  val Deadline = "deadline"
  val Email = "email"
  val Grade = "grade"
  val GroupId = "groupId"
  val Id = "id"
  val Name = "name"
  val Organizations = "organizations"
  val Picture = "picture"
  val Status = "status"
  val SubmittedCount = "submittedCount"
  val Title = "title"
  val Url = "url"
  val UserIds = "userIds"
  val Users = "users"
  val Submissions = "submissions"
}

object AssignmentMessageFields {
  val Action = "action"
  val AssignmentId = "assignmentId"
  val AssignmentIds = "assignmentIds"
  val GroupId = "groupId"
  val Order = "order"
  val Skip = "skip"
  val SortBy = "sortBy"
  val Status = "status"
  val Take = "take"
  val TitlePattern = "titlePattern"
  val UserId = "userId"
  val Json = "json"
}

object AssignmentMessageActionType extends Enumeration {
  val AssignmentUsers = Value
  val ById = Value
  val Check = Value
  val EvaluationDate = Value
  val List = Value
  val SubmissionStatus = Value
  val UserAssignments = Value
  val UserSubmissionsByPeriod = Value
  val ByIds = Value
}