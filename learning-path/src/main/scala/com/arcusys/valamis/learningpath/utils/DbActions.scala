package com.arcusys.valamis.learningpath.utils

import com.arcusys.valamis.learningpath.services.impl.actions._
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

import scala.concurrent.ExecutionContext

/**
  * Created by mminin on 13/03/2017.
  */
class DbActions(val db: JdbcBackend#DatabaseDef,
                val profile: JdbcProfile)
               (implicit executionContext: ExecutionContext){

  val versionDBIO = new VersionDBIOActions(profile)

  val learningPathDBIO = new LearningPathDBIOActions(profile)

  val goalDBIO = new GoalDBIOActions(profile)
  val goalGroupDBIO = new GoalGroupDBIOActions(profile)

  val goalActivityDBIO = new GoalLRActivityDBIOActions(profile)
  val goalLessonDBIO = new GoalLessonDBIOActions(profile)
  val goalLRActivityDBIO = new GoalLRActivityDBIOActions(profile)
  val goalAssignmentDBIO = new GoalAssignmentDBIOActions(profile)
  val goalStatementDBIO = new GoalStatementDBIOActions(profile)
  val goalWebContentDBIO = new GoalWebContentDBIOActions(profile)
  val goalTrainingEventDBIO = new GoalTrainingEventDBIOActions(profile)
  val goalCourseDBIO = new GoalCourseDBIOActions(profile)

  val userGoalStatusDBIO = new UserGoalStatusDBIOActions(profile)
  val userLPStatusDBIO = new UserLPStatusDBIOActions(profile)

  val userMemberDBIO = new UserMemberDBIOActions(profile)
  val memberDBIO = new MemberDBIOActions(profile)

  val historyDBIOActions = new LPHistoryDBIOActions(profile)

  val recommendedCompetenceDBIOActions = new RecommendedCompetenceDBIOActions(profile)
  val improvingCompetenceDBIOActions = new ImprovingCompetenceDBIOActions(profile)
}

trait DbActionsSupport {

  protected val dbActions: DbActions

  val db: JdbcBackend#DatabaseDef = dbActions.db
  val profile: JdbcProfile = dbActions.profile

  def versionDBIO: VersionDBIOActions = dbActions.versionDBIO

  def learningPathDBIO: LearningPathDBIOActions = dbActions.learningPathDBIO

  def goalDBIO: GoalDBIOActions = dbActions.goalDBIO
  def goalGroupDBIO: GoalGroupDBIOActions = dbActions.goalGroupDBIO

  def goalActivityDBIO: GoalLRActivityDBIOActions = dbActions.goalActivityDBIO
  def goalLessonDBIO: GoalLessonDBIOActions = dbActions.goalLessonDBIO
  def goalLRActivityDBIO: GoalLRActivityDBIOActions = dbActions.goalLRActivityDBIO
  def goalAssignmentDBIO: GoalAssignmentDBIOActions = dbActions.goalAssignmentDBIO
  def goalStatementDBIO: GoalStatementDBIOActions = dbActions.goalStatementDBIO
  def goalWebContentDBIO: GoalWebContentDBIOActions = dbActions.goalWebContentDBIO
  def goalTrainingEventDBIO: GoalTrainingEventDBIOActions = dbActions.goalTrainingEventDBIO
  def goalCourseDBIO: GoalCourseDBIOActions = dbActions.goalCourseDBIO

  def userGoalStatusDBIO: UserGoalStatusDBIOActions = dbActions.userGoalStatusDBIO
  def userLPStatusDBIO: UserLPStatusDBIOActions = dbActions.userLPStatusDBIO

  def userMemberDBIO: UserMemberDBIOActions = dbActions.userMemberDBIO
  def memberDBIO: MemberDBIOActions = dbActions.memberDBIO

  def historyDBIOActions: LPHistoryDBIOActions = dbActions.historyDBIOActions
}