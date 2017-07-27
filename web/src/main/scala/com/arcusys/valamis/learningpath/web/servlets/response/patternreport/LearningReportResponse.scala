package com.arcusys.valamis.learningpath.web.servlets.response.patternreport

import com.arcusys.valamis.learningpath.models._
import com.arcusys.valamis.learningpath.models.patternreport.{PathReportResult, PathsReportStatus}
import org.joda.time.DateTime

/**
  * Created by amikhailov on 23.03.17.
  */


trait BaseGoalPathsResponse {
  def id: Long

  def goalType: Int

  def isOptional: Boolean

  def title: String
}

case class ActivityGoalPathsResponse(id: Long,
                                     goalType: Int,
                                     isOptional: Boolean,
                                     title: String,
                                     activityName: String
                                    ) extends BaseGoalPathsResponse {
  def this(goal: Goal, goalLRActivity: GoalLRActivity, title: String) = this(
    goal.id,
    PathsGoalType.Activity.id,
    goal.optional,
    title,
    goalLRActivity.activityName
  )
}

case class LessonGoalPathsResponse(id: Long,
                                   goalType: Int,
                                   isOptional: Boolean,
                                   title: String,
                                   lessonId: Long
                                  ) extends BaseGoalPathsResponse {
  def this(goal: Goal, goalLesson: GoalLesson, title: String) = this(
    goal.id,
    PathsGoalType.Package.id,
    goal.optional,
    title,
    goalLesson.lessonId
  )
}

case class AssignmentGoalPathsResponse(id: Long,
                                       goalType: Int,
                                       isOptional: Boolean,
                                       title: String,
                                       assignmentId: Long
                                      ) extends BaseGoalPathsResponse {
  def this(goal: Goal, goalAssignment: GoalAssignment, title: String) = this(
    goal.id,
    PathsGoalType.Assignment.id,
    goal.optional,
    title,
    goalAssignment.assignmentId
  )
}

case class EventGoalPathsResponse(id: Long,
                                  goalType: Int,
                                  isOptional: Boolean,
                                  title: String,
                                  eventId: Long,
                                  startTime: DateTime,
                                  endTime: DateTime
                                 ) extends BaseGoalPathsResponse {
  def this(goal: Goal, goalTrainingEvent: GoalTrainingEvent, title: String) = this(
    goal.id,
    PathsGoalType.Event.id,
    goal.optional,
    title,
    goalTrainingEvent.trainingEventId,
    DateTime.now,
    DateTime.now
  )
}

case class StatementGoalPathsResponse(id: Long,
                                      goalType: Int,
                                      isOptional: Boolean,
                                      title: String,
                                      obj: String,
                                      verb: String
                                     ) extends BaseGoalPathsResponse {
  def this(goal: Goal, goalStatement: GoalStatement, title: String) = this(
    goal.id,
    PathsGoalType.Statement.id,
    goal.optional,
    title,
    goalStatement.objectId,
    goalStatement.verbId
  )
}

case class CourseGoalPathsResponse(id: Long,
                                   goalType: Int,
                                   isOptional: Boolean,
                                   title: String,
                                   courseId: Long
                                  ) extends BaseGoalPathsResponse {
  def this(goal: Goal, goalCourse: GoalCourse, title: String) = this(
    goal.id,
    PathsGoalType.Course.id,
    goal.optional,
    title,
    goalCourse.courseId
  )
}

case class WebContentGoalPathsResponse(id: Long,
                                   goalType: Int,
                                   isOptional: Boolean,
                                   title: String,
                                   webContentId: Long
                                  ) extends BaseGoalPathsResponse {
  def this(goal: Goal, goalWebContent: GoalWebContent, title: String) = this(
    goal.id,
    PathsGoalType.WebContent.id,
    goal.optional,
    title,
    goalWebContent.webContentId
  )
}

case class CertificatePathsResponse(id: Long,
                                    title: String,
                                    creationDate: DateTime,
                                    goals: Seq[_]
                                   )

case class UserCertificateResponse(id: Long,
                                   user: UserResponse,
                                   organizations: Seq[String],
                                   certificates: Seq[PathReportResult]
                                  )


case class UserResponse(id: Long,
                        name: String,
                        email: String,
                        picture: String = "",
                        pageUrl: String = ""
                       )

object PathsGoalType extends Enumeration {
  val Empty = Value(0)
  val Activity = Value(1)
  val Course = Value(2)
  val Statement = Value(3)
  val Package = Value(4)
  val Assignment = Value(5)
  val Event = Value(6)
  val WebContent = Value(7)
}

case class TotalResponse[T](id: Long, total: Map[T, Int])

case class PathReportDetailedResult(certificateId: Long,
                                    userId: Long,
                                    goals: Seq[PathGoalReportResult])

case class PathGoalReportResult(goalId: Long,
                                date: DateTime,
                                status: PathsReportStatus.Value)


case class UserCertificateStatusResponse(certificateId: Long,
                                         userId: Long,
                                         status: Int)
