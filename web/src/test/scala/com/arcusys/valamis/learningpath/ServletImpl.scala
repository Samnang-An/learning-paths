package com.arcusys.valamis.learningpath

import javax.servlet.http.HttpServletRequest

import com.arcusys.valamis.learningpath.listeners.StatementListener
import com.arcusys.valamis.learningpath.services._
import com.arcusys.valamis.learningpath.services.impl._
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickDBInfo
import com.arcusys.valamis.learningpath.tasks.TaskManager
import com.arcusys.valamis.learningpath.utils.DbActions
import com.arcusys.valamis.learningpath.web.impl._
import com.arcusys.valamis.learningpath.web.servlets.MainServletBase
import com.arcusys.valamis.learningpath.web.servlets.base.{AuthStrategy, PermissionChecker}
import com.arcusys.valamis.learningpath.web.servlets.utils.{FileStorageMemoryImpl, LRActivityTypeServiceImpl, LoggerImpl}
import com.arcusys.valamis.lrssupport.lrs.service.{LrsClientManager, LrsRegistration}
import com.arcusys.valamis.members.picker.service.{LiferayHelper, MemberService}
import org.apache.commons.logging.Log
import org.joda.time.DateTime
import org.scalatra.ScalatraBase

import scala.concurrent.ExecutionContext

/**
  * Servlet implementation for unit test environment
  */
class ServletImpl(val dbInfo: SlickDBInfo) extends MainServletBase("/") {

  override implicit val executionContext: ExecutionContext = ExecutionContext.global

  override val dbActions: DbActions = new DbActions(dbInfo.db, dbInfo.profile)

  override implicit def companyId: Long = {
    request.header("companyId").map(_.toLong).getOrElse(-1)
  }

  override def getHomePage(companyId: Long): String = "http://localhost"

  override protected val scentryConfig = LiferayAuthConfig.asInstanceOf[ScentryConfiguration]

  protected def authStrategy(app: ScalatraBase): AuthStrategy = new AuthStrategyTestImpl(app)

  override val logoFileStorage: FileStorage = new FileStorageMemoryImpl()

  override val trainingEventServiceBridge = new TrainingEventServiceBridgeTestImpl

  val lrActivityTypeService = new LRActivityTypeServiceImpl()

  val assetService = new AssetServiceTestImpl

  override def liferayHelper: LiferayHelper = new LiferayHelperTestImpl(companyId)

  override protected def log: Log = new LoggerImpl

  override protected def getMimeType(fileName: String): String = "image/png"

  override def courseService: CourseService = new CourseServiceTestImpl

  private def courseUserStatusService = new CourseUserStatusServiceImpl(messageBusService)

  def now: DateTime = DateTime.now

  override def taskManager: TaskManager =
    new SimpleTaskManagerImpl(dbInfo.db, dbInfo.profile, log, dbActions,
      lrActivityTypeService, lessonService, courseUserStatusService,
      trainingEventServiceBridge, messageBusService,
      userLPStatusModelListener, now)

  override lazy val messageBusService: MessageBusService = new MessageBusServiceTestImpl(
    Map(), Map(), true,
    Map(), Map(), true,
    Map(), true
  )

  override protected val webContentService = new WebContentServiceImpl

  override def getPortalURL(request: HttpServletRequest): String = "http://localhost"

  override def valamisContextPath: String = "/learn-portlet/"

  override def permissionChecker: PermissionChecker = new PermissionCheckerTestImpl()

  override def userService = new UserServiceImplTest(liferayHelper)

  def certificateTracker: CertificateTracker = ???

  override def lrsClient: LrsClientManager = new LrsClientTestImpl

  def lpStatementService: LPStatementService = new LpStatementServiceTestImpl

  lazy val learningPathModelListener: LPModelListener =
    new LPModelListener(dbActions, assetService, messageBusService, log)
  lazy val userLPStatusModelListener: UserLPStatusModelListener =
    new UserLPStatusModelListener(dbActions)
  lazy val statementListener: StatementListener =
    new StatementListener(dbActions, taskManager)

  lazy val socialActivityHelper: SocialActivityHelper = new SocialActivityHelperTestImpl

  override def learningPathService: LearningPathService =
    new LearningPathServiceImpl(dbActions, dbActions.improvingCompetenceDBIOActions,
      dbActions.recommendedCompetenceDBIOActions,logoFileStorage, memberService, taskManager,
      certificateNotificationService, learningPathModelListener, userLPStatusModelListener,
      socialActivityHelper, lpStatementService)

  override def memberService: MemberService = new LPMemberServiceImpl(
    dbInfo.db,
    dbInfo.profile,
    dbActions,
    liferayHelper,
    taskManager,
    certificateNotificationService,
    userLPStatusModelListener,
    lpStatementService
  )

  override protected def certificateNotificationService: CertificateNotificationService =
    new CertificateNotificationServiceTestImpl

  override def lrsRegistration: LrsRegistration =
    throw new NotImplementedError()//not used in tests

  override protected def groupService: GroupService =
    new GroupServiceTestImpl(Map())
}
