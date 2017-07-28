package com.arcusys.valamis.learningpath.tasks

import com.arcusys.valamis.learningpath.models.{Goal, GoalTypes, StatementInfo}

trait TaskManager {

  def planLearningPathCheckerTask(versionId: Long,
                                  userId: Long)
                                 (implicit companyId: Long): Unit

  def planUndefinedStatusChecker(learningPathId: Long)
                                (implicit companyId: Long): Unit


  def planLRActivityGoalCheckerTask(goalId: Long,
                                    userId: Long)
                                   (implicit companyId: Long): Unit

  def planLessonGoalCheckerTask(goalId: Long,
                                userId: Long)
                               (implicit companyId: Long): Unit

  def planAssignmentGoalCheckerTask(goalId: Long,
                                    userId: Long)
                                   (implicit companyId: Long): Unit

  def planCourseGoalCheckerTask(goalId: Long, userId: Long)
                               (implicit companyId: Long): Unit

  def planTrainingEventGoalCheckerTask(goalId: Long,
                                       userId: Long)
                                      (implicit companyId: Long): Unit

  def planGroupGoalCheckerTask(groupId: Long,
                               userId: Long)
                              (implicit companyId: Long): Unit

  def planStatementGoalCheckerTask(goalId: Long,
                                   userId: Long,
                                   statement: StatementInfo)
                                  (implicit companyId: Long): Unit

  def planWebContentGoalCheckerTask(goalId: Long,
                                    userId: Long,
                                    viewed: Boolean = false)
                                   (implicit companyId: Long): Unit

  def planGoalChecker(goal: Goal, userId: Long)
                     (implicit companyId: Long): Unit = goal.goalType match {
    case GoalTypes.LRActivity =>
      planLRActivityGoalCheckerTask(goal.id, userId)
    case GoalTypes.Lesson =>
      planLessonGoalCheckerTask(goal.id, userId)
    case GoalTypes.Assignment =>
      planAssignmentGoalCheckerTask(goal.id, userId)
    case GoalTypes.Course =>
      planCourseGoalCheckerTask(goal.id, userId)
    case GoalTypes.Group =>
      planGroupGoalCheckerTask(goal.id, userId)
    case GoalTypes.WebContent =>
      planWebContentGoalCheckerTask(goal.id, userId)
    case GoalTypes.TrainingEvent =>
      planTrainingEventGoalCheckerTask(goal.id, userId)
    // TODO: add statement type support
    //case GoalTypes.Statement =>
    //  taskManager.
    case _ =>
  }

}
