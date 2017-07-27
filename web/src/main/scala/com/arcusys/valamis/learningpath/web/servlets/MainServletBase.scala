package com.arcusys.valamis.learningpath.web.servlets

import com.arcusys.valamis.learningpath.models.{CertificateStatuses, GoalStatuses, GoalTypes}
import com.arcusys.valamis.learningpath.services._
import com.arcusys.valamis.learningpath.services.impl._
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickDBInfo
import com.arcusys.valamis.learningpath.tasks.TaskManager
import com.arcusys.valamis.learningpath.utils.DbActions
import com.arcusys.valamis.learningpath.web.servlets.base.LearningPathServletBase
import com.arcusys.valamis.learningpath.web.servlets.utils.EnumNameKeySerializer
import com.arcusys.valamis.lrssupport.lrs.service.LrsClientManager
import com.arcusys.valamis.members.picker.model.MemberTypes
import com.arcusys.valamis.members.picker.service.LiferayHelper
import org.json4s.ext.{EnumNameSerializer, JodaTimeSerializers}
import org.json4s.{DefaultFormats, Formats}


/**
  * Created by mminin on 26/01/2017.
  */
abstract class MainServletBase(prefix: String) extends {

  protected val learningPathsPrefix: String = prefix + "learning-paths"
  protected val versionsPrefix: String = prefix + "versions"
  protected val goalsPrefix: String = prefix + "goals"
  protected val groupsPrefix: String = prefix + "goal-groups"
  protected val logoFilesPrefix: String = prefix + "logo-files"
  protected val userPrefix: String = prefix + "users"
  protected val activitiesPrefix: String = prefix + "lr-activity-types"
  protected val coursesPrefix: String = prefix + "courses"
  protected val webContentPrefix: String = prefix + "web-contents"
  protected val lrsEndpointPrefix: String = prefix + "lrs-settings"
  protected val expiredPrefix: String = prefix + "expired-certificates"
  protected val learningPatternReportPrefix: String = prefix + "learning-pattern-report"
  protected val statisticReportPrefix: String = prefix + "learning-statistics-report"
  protected val competencesCertificatesPrefix: String = prefix + "competences-certificates"
  protected val lfGroupsPrefix: String = prefix + "groups"

} with LearningPathServletBase
  with LearningPathsServlet
  with LearningPathsLogoServlet
  with VersionServlet
  with CoursesServlet
  with MembersServlet
  with GoalsGroupServlet
  with GoalsServlet
  with UserServlet
  with LRActivitiesServlet
  with WebContentServlet
  with LrsEndpointServlet
  with PermissionFilter
  with CertificateTrackerServlet
  with LearningPatternReportServlet
  with StatisticReportServlet
  with CompetencesCertificatesServlet
  with GroupServlet {

  override implicit def jsonFormats: Formats = DefaultFormats ++
    JodaTimeSerializers.all +
    new EnumNameSerializer(GoalTypes) +
    new EnumNameSerializer(MemberTypes) +
    new EnumNameSerializer(GoalStatuses) +
    new EnumNameKeySerializer(GoalStatuses) +
    new EnumNameKeySerializer(CertificateStatuses)


  def logoFileStorage: FileStorage
  def liferayHelper: LiferayHelper
  def taskManager: TaskManager
  def dbActions: DbActions
  def messageBusService: MessageBusService
  def courseService: CourseService
  def trainingEventServiceBridge: TrainingEventServiceBridge
  def lrsClient: LrsClientManager

  lazy val versionService: LPVersionService =
    new LPVersionServiceImpl(dbActions, dbActions.improvingCompetenceDBIOActions,
      dbActions.recommendedCompetenceDBIOActions,
      logoFileStorage, memberService)
  lazy val logoService: LPLogoService =
    new LPLogoServiceImpl(dbActions, logoFileStorage)

  lazy val goalService: GoalService =
    new GoalServiceImpl(dbActions, lessonService, webContentService, courseService,
      trainingEventServiceBridge, lrsClient, messageBusService)(executionContext, log)
  lazy val goalGroupService: GoalsGroupService =
    new GoalsGroupServiceImpl(dbActions)
  lazy val goalLessonService: GoalLessonService =
    new GoalLessonServiceImpl(dbActions, lessonService)
  lazy val goalActivityService: GoalActivityService =
    new GoalActivityServiceImpl(dbActions)
  lazy val goalAssignmentService: GoalAssignmentService =
    new GoalAssignmentServiceImpl(dbActions, messageBusService)
  lazy val goalWebContentService: GoalWebContentService =
    new GoalWebContentServiceImpl(dbActions, webContentService)
  lazy val goalTrainingEventService: GoalTrainingEventService =
    new GoalTrainingEventServiceImpl(dbActions, trainingEventServiceBridge)
  lazy  val goalStatementService: GoalStatementService =
    new GoalStatementServiceImpl(dbActions)
  lazy val goalCourseService: GoalCourseService =
    new GoalCourseServiceImpl(dbActions, courseService)


  lazy val userProgressService: UserProgressService =
    new UserProgressServiceImpl(dbActions)

  lazy val lessonService: LessonService = new LessonServiceImpl(messageBusService)

  lazy val patternReportService: LearningPathsReportService =
    new LearningPathsReportServiceImpl(dbActions, learningPathService, goalService, memberService, userProgressService)

  lazy val recommendedCompetenceService: CompetenceService = new CompetenceServiceImpl(dbActions,
    dbActions.recommendedCompetenceDBIOActions
  )

  lazy val improvingCompetenceService: CompetenceService = new CompetenceServiceImpl(dbActions,
    dbActions.improvingCompetenceDBIOActions
  )

  def dbInfo: SlickDBInfo
}
