package com.arcusys.valamis.learningpath.tasks

import com.arcusys.valamis.learningpath.listeners.UserLPStatusListener
import com.arcusys.valamis.learningpath.models.StatementInfo
import com.arcusys.valamis.learningpath.services._
import com.arcusys.valamis.learningpath.services.impl.UserLPStatusModelListener
import com.arcusys.valamis.learningpath.utils.DbActions
import org.apache.commons.logging.Log
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}

trait TaskManagerBaseImpl extends TaskManager {

  object TaskManagerKeys {
    val learningPathId = "learningPathId"
    val companyId = "companyId"
    val userId = "userId"
    val groupId = "groupId"
    val goalId = "goalId"
    val versionId = "versionId"
    val viewed = "viewed"
    val verbId = "verbId"
    val objectId = "objectId"
    val timeStampId = "timeStampId"
    val now = "now"
  }

  protected def log: Log

  protected def dbActions: DbActions

  protected def lrActivityService: LRActivityTypeService

  protected def lessonService: LessonService

  protected def messageBusService: MessageBusService

  protected def courseUserStatusService: CourseUserStatusService

  protected def trainingEventServiceBridge: TrainingEventServiceBridge

  protected def userLPStatusModelListener: UserLPStatusModelListener
  protected def userLPStatusListener: UserLPStatusListener

  implicit def executionContext: ExecutionContext


  def planLearningPathCheckerTask(versionId: Long,
                                  userId: Long)
                                 (implicit companyId: Long): Unit = {
    planTask(Tasks.CheckLearningPathStatus, Map(
      TaskManagerKeys.userId -> userId,
      TaskManagerKeys.versionId -> versionId
    ))
  }

  def planUndefinedStatusChecker(learningPathId: Long)
                                (implicit companyId: Long): Unit = {
    planTask(Tasks.CheckLearningPathGoals, Map(
      TaskManagerKeys.learningPathId -> learningPathId
    ))
  }

  private def planGoalTask(name: String,
                           userId: Long,
                           goalId: Long)
                          (implicit companyId: Long): Unit = {
    planTask(name, Map(
      TaskManagerKeys.userId -> userId,
      TaskManagerKeys.goalId -> goalId
    ))
  }

  def planLRActivityGoalCheckerTask(goalId: Long,
                                    userId: Long)
                                   (implicit companyId: Long): Unit = {
    planGoalTask(Tasks.CheckLRActivityGoal, userId, goalId)
  }

  def planLessonGoalCheckerTask(goalId: Long,
                                userId: Long)
                               (implicit companyId: Long): Unit = {
    planGoalTask(Tasks.CheckLessonGoal, userId, goalId)
  }

  def planAssignmentGoalCheckerTask(goalId: Long,
                                    userId: Long)
                                   (implicit companyId: Long): Unit = {
    planGoalTask(Tasks.CheckAssignmentGoal, userId, goalId)
  }

  def planCourseGoalCheckerTask(goalId: Long, userId: Long)
                               (implicit companyId: Long): Unit = {
    planGoalTask(Tasks.CheckCourseGoal, userId, goalId)
  }

  def planTrainingEventGoalCheckerTask(goalId: Long,
                                       userId: Long)
                                      (implicit companyId: Long): Unit = {
    planGoalTask(Tasks.CheckTrainingEventGoal, userId, goalId)
  }


  def planGroupGoalCheckerTask(groupId: Long,
                               userId: Long)
                              (implicit companyId: Long): Unit = {
    planTask(Tasks.CheckGroupsGoal, Map(
      TaskManagerKeys.userId -> userId,
      TaskManagerKeys.groupId -> groupId
    ))
  }

  def planStatementGoalCheckerTask(goalId: Long,
                                   userId: Long,
                                   statement: StatementInfo)
                                  (implicit companyId: Long): Unit = {
    planTask(Tasks.CheckStatementGoal, Map(
      TaskManagerKeys.userId -> userId,
      TaskManagerKeys.goalId -> goalId,
      TaskManagerKeys.verbId -> statement.verbId,
      TaskManagerKeys.objectId -> statement.objectId,
      TaskManagerKeys.timeStampId -> statement.timeStamp
    ))
  }


  def planWebContentGoalCheckerTask(goalId: Long,
                                    userId: Long,
                                    viewed: Boolean = false)
                                   (implicit companyId: Long): Unit = {
    planTask(Tasks.CheckWebContentGoal, Map(
      TaskManagerKeys.userId -> userId,
      TaskManagerKeys.goalId -> goalId,
      TaskManagerKeys.viewed -> viewed
    ))
  }


  /**
    * impl should add task to queue and use 'runTask' to run it
    *
    * TODO: try to use case classes instead name and data
    */
  protected def planTask(name: String, data: Map[String, java.io.Serializable])
                        (implicit companyId: Long): Unit


  private def runGoalTask(data: Map[String, Any], checker: GoalCheckerBase)
                         (implicit companyId: Long): Future[Unit] = {

    val userId = data(TaskManagerKeys.userId).toString.toLong
    val goalId = data(TaskManagerKeys.goalId).toString.toLong
    val now = data.get(TaskManagerKeys.now) map (_.asInstanceOf[DateTime]) //used in tests

    checker.checkGoal(goalId, userId, now)
  }

  def runTask(name: String, data: Map[String, Any])
             (implicit companyId: Long): Future[Unit] = {
    val action = name match {
      case Tasks.CheckLearningPathStatus =>
        val userId = data(TaskManagerKeys.userId).toString.toLong
        val versionId = data(TaskManagerKeys.versionId).toString.toLong

        new LearningPathChecker(dbActions, userLPStatusModelListener, userLPStatusListener)
          .checkLearningPath(versionId, userId)

      case Tasks.CheckLearningPathGoals =>
        val learningPathId = data(TaskManagerKeys.learningPathId).toString.toLong

        new UndefinedStatusesChecker(dbActions, this)
          .checkLearningPath(learningPathId)

      case Tasks.CheckLRActivityGoal =>
        runGoalTask(data, new LRActivityGoalChecker(dbActions, lrActivityService, this))

      case Tasks.CheckLessonGoal =>
        runGoalTask(data, new LessonGoalChecker(dbActions, lessonService, this))

      case Tasks.CheckAssignmentGoal =>
        runGoalTask(data, new AssignmentGoalChecker(dbActions, messageBusService, this))

      case Tasks.CheckCourseGoal =>
        runGoalTask(data, new CourseGoalChecker(dbActions, courseUserStatusService, this))

      case Tasks.CheckTrainingEventGoal =>
        runGoalTask(data,
          new TrainingEventGoalChecker(dbActions, trainingEventServiceBridge, messageBusService, this)
        )

      case Tasks.CheckWebContentGoal =>
        val userId = data(TaskManagerKeys.userId).toString.toLong
        val goalId = data(TaskManagerKeys.goalId).toString.toLong
        val viewed = data(TaskManagerKeys.viewed).toString.toBoolean

        new WebContentGoalChecker(dbActions, this)
          .checkGoal(goalId, userId, viewed)

      case Tasks.CheckStatementGoal =>
        val userId = data(TaskManagerKeys.userId).toString.toLong
        val goalId = data(TaskManagerKeys.goalId).toString.toLong
        val statementInfo = StatementInfo(
          data(TaskManagerKeys.verbId).toString,
          data(TaskManagerKeys.objectId).toString,
          DateTime.parse(data(TaskManagerKeys.timeStampId).toString)
        )

        new StatementGoalChecker(dbActions, this)
          .checkGoal(goalId, userId, statementInfo)

      case Tasks.CheckGroupsGoal =>
        val userId = data(TaskManagerKeys.userId).toString.toLong
        val groupId = data(TaskManagerKeys.groupId).toString.toLong
        val now = data.get(TaskManagerKeys.now) map (_.asInstanceOf[DateTime]) //used in tests

        new GroupsGoalChecker(dbActions, this)
          .checkGoal(groupId, userId, now)
    }

    action.recover {
      case e: TaskAbortException => log.info(s"task '$name' aborted, ${e.getMessage}")
    }
  }
}
