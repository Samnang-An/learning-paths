package com.arcusys.valamis.learningpath.models

import org.joda.time.{DateTime, Period}

/**
  * Created by mminin on 01/02/2017.
  */
object GoalTypes extends Enumeration {
  val Group = Value("group")
  val Lesson = Value("lesson")
  val LRActivity = Value("activity")
  val Assignment = Value("assignment")
  val WebContent = Value("webContent")
  val TrainingEvent = Value("trainingEvent")
  val Statement = Value("statement")
  val Course = Value("course")
}

/**
  * @param oldGoalId link to previous version of goal
  */
case class Goal(id: Long,
                oldGoalId: Option[Long],
                versionId: Long,
                groupId: Option[Long],
                goalType: GoalTypes.Value,
                indexNumber: Int,
                timeLimit: Option[Period],
                optional: Boolean = false,
                modifiedDate: DateTime)

case class GoalGroup(goalId: Long,
                     title: String,
                     count: Option[Int])

case class GoalLesson(goalId: Long,
                      lessonId: Long)

case class GoalAssignment(goalId: Long,
                          assignmentId: Long)

case class GoalLRActivity(goalId: Long,
                          activityName: String,
                          count: Int)

case class GoalTrainingEvent(goalId: Long,
                             trainingEventId: Long)

case class GoalStatement(goalId: Long,
                         verbId: String,
                         objectId: String,
                         objectName: String)

case class GoalWebContent(goalId: Long,
                          webContentId: Long)

case class GoalCourse(goalId: Long,
                      courseId: Long)

case class GoalsSet(groups: Seq[(Goal, GoalGroup)],
                    lessons: Seq[(Goal, GoalLesson, String)],
                    lrActivities: Seq[(Goal, GoalLRActivity)],
                    assignments: Seq[(Goal, GoalAssignment, String)],
                    webContents: Seq[(Goal, GoalWebContent, String)],
                    trainingEvents: Seq[(Goal, GoalTrainingEvent, String)],
                    statements: Seq[(Goal, GoalStatement)],
                    courses: Seq[(Goal, GoalCourse, String)])

