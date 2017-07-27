package com.arcusys.valamis.learningpath.services.impl.utils

import com.arcusys.valamis.learningpath.models._
import com.arcusys.valamis.learningpath.services.impl.actions._
import org.joda.time.DateTime
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

trait CopyGoalsSupport {

  protected implicit val executionContext: ExecutionContext

  protected def goalGroupDBIO: GoalGroupDBIOActions
  protected def goalActivityDBIO: GoalLRActivityDBIOActions
  protected def goalLessonDBIO: GoalLessonDBIOActions
  protected def goalLRActivityDBIO: GoalLRActivityDBIOActions
  protected def goalAssignmentDBIO: GoalAssignmentDBIOActions
  protected def goalStatementDBIO: GoalStatementDBIOActions
  protected def goalWebContentDBIO: GoalWebContentDBIOActions
  protected def goalTrainingEventDBIO: GoalTrainingEventDBIOActions
  protected def goalCourseDBIO: GoalCourseDBIOActions


  protected def copyGoals(sourceVersionId: Long,
                        targetVersionId: Long,
                        now: DateTime): DBIO[_] = {
    for {
      groups <- goalGroupDBIO.getWithGoalInfoByVersionId(sourceVersionId)
      activityGoals <- goalActivityDBIO.getWithGoalInfoByVersionId(sourceVersionId)
      lessonGoals <- goalLessonDBIO.getWithGoalInfoByVersionId(sourceVersionId)
      assignmentGoals <- goalAssignmentDBIO.getWithGoalInfoByVersionId(sourceVersionId)
      statementGoals <- goalStatementDBIO.getWithGoalInfoByVersionId(sourceVersionId)
      webContentGoals <- goalWebContentDBIO.getWithGoalInfoByVersionId(sourceVersionId)
      trainingEventsGoals <- goalTrainingEventDBIO.getWithGoalInfoByVersionId(sourceVersionId)
      courseGoals <- goalCourseDBIO.getWithGoalInfoByVersionId(sourceVersionId)
      _ <- copyGoalsGroupContent(None, targetVersionId, None, groups,
        activityGoals, lessonGoals, assignmentGoals,
        statementGoals, webContentGoals, trainingEventsGoals, courseGoals, now)
    } yield ()
  }

  private def goalActions[T](groupId: Option[Long],
                             newVersionId: Long,
                             newGroupId: Option[Long],
                             goals: Seq[(Goal, T)],
                             now: DateTime)
                            (insertAction: (Long, T) => DBIO[Int]) = {
    goals.filter { case (goal, _) => goal.groupId == groupId }
      .map { case (goal, goalInfo) =>
        for {
          newGoalId <- createNewGoalVersion(newVersionId, newGroupId, goal, now)
          _ <- insertAction(newGoalId, goalInfo)
        } yield {}
      }
  }

  private def copyGoalsGroupContent(groupId: Option[Long],
                                    newVersionId: Long,
                                    newGroupId: Option[Long],
                                    groups: Seq[(Goal, GoalGroup)],
                                    activityGoals: Seq[(Goal, GoalLRActivity)],
                                    lessonGoals: Seq[(Goal, GoalLesson)],
                                    assignmentGoals: Seq[(Goal, GoalAssignment)],
                                    statementGoals: Seq[(Goal, GoalStatement)],
                                    webContentsGoals: Seq[(Goal, GoalWebContent)],
                                    trainingEventsGoals: Seq[(Goal, GoalTrainingEvent)],
                                    courseGoals: Seq[(Goal, GoalCourse)],
                                    now: DateTime): DBIO[_] = {
    val activityActions =
      goalActions(groupId, newVersionId, newGroupId, activityGoals, now) { (newGoalId, goalInfo) =>
        goalActivityDBIO.insert(GoalLRActivity(newGoalId, goalInfo.activityName, goalInfo.count))
      }

    val lessonActions =
      goalActions(groupId, newVersionId, newGroupId, lessonGoals, now) { (newGoalId, goalInfo) =>
        goalLessonDBIO.insert(GoalLesson(newGoalId, goalInfo.lessonId))
      }

    val assignmentActions =
      goalActions(groupId, newVersionId, newGroupId, assignmentGoals, now) { (newGoalId, goalInfo) =>
        goalAssignmentDBIO.insert(GoalAssignment(newGoalId, goalInfo.assignmentId))
      }

    val statementAction =
      goalActions(groupId, newVersionId, newGroupId, statementGoals, now) { (newGoalId, goalInfo) =>
        goalStatementDBIO.insert{
          GoalStatement(newGoalId, goalInfo.verbId, goalInfo.objectId, goalInfo.objectName)
        }
      }

    val webContentAction =
      goalActions(groupId, newVersionId, newGroupId, webContentsGoals, now) { (newGoalId, goalInfo) =>
        goalWebContentDBIO.insert(GoalWebContent(newGoalId, goalInfo.webContentId))
      }

    val trainingEventsAction =
      goalActions(groupId, newVersionId, newGroupId, trainingEventsGoals, now) { (newGoalId, goalInfo) =>
        goalTrainingEventDBIO.insert(GoalTrainingEvent(newGoalId, goalInfo.trainingEventId))
      }

    val courseGoalsAction =
      goalActions(groupId, newVersionId, newGroupId, courseGoals, now) { (newGoalId, goalInfo) =>
        goalCourseDBIO.insert(GoalCourse(newGoalId, goalInfo.courseId))
      }

    val groupActions = groups
      .filter { case (goal, _) => goal.groupId == groupId }
      .map { case (goal, goalInfo) =>
        for {
          id <- createNewGoalVersion(newVersionId, newGroupId, goal, now)
          _ <- goalGroupDBIO.insert(GoalGroup(id, goalInfo.title, goalInfo.count))
          _ <- copyGoalsGroupContent(Some(goal.id), newVersionId, Some(id), groups,
            activityGoals, lessonGoals, assignmentGoals,
            statementGoals, webContentsGoals, trainingEventsGoals, courseGoals, now)
        } yield {}
      }

    DBIO.seq(activityActions ++ lessonActions ++ assignmentActions ++
      groupActions ++ webContentAction ++ trainingEventsAction ++
      statementAction ++ courseGoalsAction: _*)
  }

  protected def createNewGoalVersion(newVersionId: Long,
                                   groupId: Option[Long],
                                   goal: Goal,
                                   now: DateTime): DBIO[Long]
}
