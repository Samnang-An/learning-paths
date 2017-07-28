package com.arcusys.valamis.learningpath.init

import javax.servlet.http.HttpServletRequest

import com.arcusys.valamis.learningpath.asset.AssetServiceImpl
import com.arcusys.valamis.learningpath.impl._
import com.arcusys.valamis.learningpath.impl.utils.LiferayLogSupport
import com.arcusys.valamis.learningpath.listeners.{UserLPStatusListener, _}
import com.arcusys.valamis.learningpath.listeners.impl.UserLPStatusListenerImpl
import com.arcusys.valamis.learningpath.models.LearningPath
import com.arcusys.valamis.learningpath.services.impl._
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickDBInfo
import com.arcusys.valamis.learningpath.services._
import com.arcusys.valamis.learningpath.utils.DbActions
import com.arcusys.valamis.lrssupport.lrs.service._
import com.arcusys.valamis.lrssupport.lrsEndpoint.service.LrsEndpointServiceImpl
import com.arcusys.valamis.lrssupport.lrsEndpoint.storage.LrsEndpointStorage
import com.arcusys.valamis.lrssupport.services.UserCredentialsStorageImpl
import com.arcusys.valamis.lrssupport.tables.{LrsEndpointStorageImpl, TokenRepositoryImpl}
import com.arcusys.valamis.message.broker.liferay700.LiferayMessageService
import com.arcusys.valamis.settings.{SettingStorage, SettingStorageImpl}
import com.arcusys.valamis.training.events.service.impl.TrainingServiceImpl
import com.liferay.portal.kernel.service.{CompanyLocalServiceUtil, ServiceContextThreadLocal}
import com.liferay.portal.kernel.util.{InfrastructureUtil, PortalUtil}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

/**
  * Created by mminin on 10/03/2017.
  */
object Configuration extends LiferayLogSupport {
  implicit val executionContext = ExecutionContext.Implicits.global

  final val LearningPathPortletId = "com_arcusys_valamis_learningpath_portlets_LearningPathsPortlet"

  val contextName = "comarcusysvalamislearningpath"
  val valamisContextPath = "/o/"

  lazy val dbInfo = SlickDBInfo.fromDataSource(InfrastructureUtil.getDataSource)
  lazy val dbActions = new DbActions(dbInfo.db, dbInfo.profile)

  lazy val lrActivityService = new LRActivityTypeServiceImpl()
  lazy val webContentService = new WebContentServiceImpl

  lazy val liferayHelper = new LiferayHelperImpl
  lazy val courseService = new CourseServiceImpl
  lazy val companyService = new CompanyServiceImpl
  lazy val assetEntryService = new AssetEntryServiceImpl
  lazy val logoFileStorage = new FileStorageImpl("valamis/lp/logo")

  lazy val messageBusService = new MessageBusServiceImpl
  lazy val messageService = new LiferayMessageService(log)

  lazy val emailNotificationHelper = new EmailNotificationHelperImpl
  lazy val userNotificationEventHelper = new UserNotificationEventLocalServiceHelperImpl


  lazy val  endpointStorageImpl = new LrsEndpointStorageImpl(dbInfo.db, dbInfo.profile)

  lazy val lrsEndpointServiceImpl= new LrsEndpointServiceImpl{
    override def endpointStorage: LrsEndpointStorage = endpointStorageImpl
  }

  lazy val lrsOAuthServiceImpl = new LrsOAuthServiceImpl {
    override def lrsTokenStorage = lrsTokenStorageImpl
  }

  lazy val lrsClientManager = new LrsClientManagerImpl with CompanyUtil {
    override def lrsEndpointService = lrsEndpointServiceImpl

    override def authCredentials = authCredentialsImpl

    override def lrsRegistration = lrsRegistrationImpl

    override def getHost(companyId: Long) = getHostWithPort(companyId)


    def getHomePage(companyId: Long): String = {
      "http://" + CompanyLocalServiceUtil.getCompany(companyId).getVirtualHostname
    }

    def getHostWithPort(companyId: Long, isSecure: Boolean = false):String = {
      lazy val company = CompanyLocalServiceUtil.getCompany(companyId)

      val hostName = company.getVirtualHostname
      val port = PortalUtil.getPortalServerPort(isSecure)
      PortalUtil.getPortalURL(hostName, port, isSecure)
    }
  }

  lazy val lrsTokenStorageImpl = new TokenRepositoryImpl(dbInfo.db,
    dbInfo.profile)

  lazy val lrsRegistrationImpl: LrsRegistration = new LrsRegistrationImpl {
    override def lrsEndpointService = lrsEndpointServiceImpl

    override def lrsOAuthService =  lrsOAuthServiceImpl

    override def lrsTokenStorage = lrsTokenStorageImpl
  }

  def authCredentialsImpl: UserCredentialsStorage = new UserCredentialsStorageImpl(getRequest)

  def getRequest: (Unit => Option[HttpServletRequest]) = _ => {
    Option(ServiceContextThreadLocal.getServiceContext).map { context =>
      context.getRequest
    }
  }


  val socialActivityHelper = new SocialActivityHelperImpl[LearningPath]
  lazy val userService = new UserServiceImpl
  lazy val groupService = new GroupServiceImpl
  lazy val memberService = new LPMemberServiceImpl(
    dbInfo.db,
    dbInfo.profile,
    dbActions,
    liferayHelper,
    taskManager,
    certificateNotificationService,
    userLPStatusModelListener,
    lpStatementService
  )



  lazy val lpStatementService = new LPStatementServiceImpl {
    override protected def lrsClientManager: LrsClientManager with CompanyUtil = Configuration.this.lrsClientManager

    override protected def lrsRegistration: LrsRegistration = Configuration.this.lrsRegistrationImpl

    override protected def userService: UserService = Configuration.this.userService
  }

  lazy val lessonService = new LessonServiceImpl(messageBusService)
  lazy val courseUserStatusService = new CourseUserStatusServiceImpl(messageBusService)

  lazy val taskManager = new TaskManagerImpl

  // TODO use OSGI-component to get TrainingServiceImpl
  lazy val trainingService = new TrainingServiceImpl(
    dbActions.db,
    dbActions.profile,
    None,
    None,
    None,
    executionContext)

  lazy val assetService: AssetService = new AssetServiceImpl

  lazy val certificateTrackerService = new CertificateTrackerImpl(dbActions,
    liferayHelper,
    certificateNotificationService)
  lazy val certificateNotificationService = new CertificateNotificationServiceImpl(emailNotificationHelper,
    userNotificationEventHelper)
  lazy val certificateShedulerService = new CertificateShedulerService(dbActions,
    certificateTrackerService,
    socialActivityHelper,
    log)

  lazy val learningPathModelListener: LPModelListener =
    new LPModelListener(dbActions, assetService, messageBusService, log)
  lazy val userLPStatusModelListener: UserLPStatusModelListener =
    new UserLPStatusModelListener(dbActions)

  lazy val userLPStatusListener: UserLPStatusListener =
    new UserLPStatusListenerImpl(Configuration.dbActions,
      improvingCompetenceService,
      Configuration.certificateNotificationService,
      Configuration.socialActivityHelper,
      Configuration.lpStatementService,
      Configuration.messageService,
      log
    )

  lazy val improvingCompetenceService: CompetenceService = new CompetenceServiceImpl(dbActions,
    dbActions.improvingCompetenceDBIOActions
  )

  lazy val learningPathService: LearningPathService = new LearningPathServiceImpl(
    dbActions,
    dbActions.improvingCompetenceDBIOActions,
    dbActions.recommendedCompetenceDBIOActions,
    logoFileStorage,
    memberService,
    taskManager,
    certificateNotificationService,
    learningPathModelListener,
    userLPStatusModelListener,
    socialActivityHelper,
    lpStatementService
  )


  lazy val trainingEventServiceBridge = new TrainingEventServiceBridgeImpl(dbActions, messageService, trainingService)

  lazy val lrActivityListener = new LRActivityListener(dbActions, taskManager)
  lazy val memberListener = new MemberListener(dbInfo.db, memberService)
  lazy val memberGroupListener = new MeberGroupsListener(dbActions, memberService)
  lazy val assignmentListener = new AssignmentListener(dbActions, taskManager)
  lazy val lessonListener = new LessonListener(dbActions, taskManager)
  lazy val courseListener = new CourseListener(dbActions, taskManager)
  lazy val trainingEventListener = new TrainingEventListener(dbActions, taskManager)
  lazy val webContentListener = new WebContentListener(dbActions, taskManager, webContentService)
  lazy val learningPathListener =
    new LearningPathListener(dbActions, learningPathService, taskManager, "delegate/learning-paths/logo-files")

  lazy val settingStorage: SettingStorage = new SettingStorageImpl(dbInfo.db, dbInfo.profile)
}
