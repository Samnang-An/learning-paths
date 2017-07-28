package com.arcusys.valamis.learningpath.servlets

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.arcusys.valamis.learningpath.impl.utils.LiferayLogSupport
import com.arcusys.valamis.learningpath.init.Configuration
import com.arcusys.valamis.learningpath.services._
import com.arcusys.valamis.learningpath.services.impl.LPMemberServiceImpl
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickDBInfo
import com.arcusys.valamis.learningpath.tasks.TaskManager
import com.arcusys.valamis.learningpath.utils.DbActions
import com.arcusys.valamis.learningpath.web.servlets.MainServletBase
import com.arcusys.valamis.lrssupport.lrs.service.{LrsClientManager, LrsClientManagerImpl, LrsRegistration, UserCredentialsStorage}
import com.arcusys.valamis.lrssupport.lrsEndpoint.service.LrsEndpointService
import com.arcusys.valamis.members.picker.service.LiferayHelper
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal
import com.liferay.portal.kernel.util.{MimeTypesUtil, PortalUtil}

import scala.concurrent.ExecutionContext

/**
  * Created by mminin on 26/01/2017.
  */
class MainServlet(prefix: String)
  extends MainServletBase(prefix)
    with LiferayAuthSupport
    with LiferayLogSupport {

  override implicit val executionContext: ExecutionContext = Configuration.executionContext

  override implicit def companyId: Long = CompanyThreadLocal.getCompanyId

  override val dbInfo: SlickDBInfo = Configuration.dbInfo
  override protected val lrActivityTypeService: LRActivityTypeService =
    Configuration.lrActivityService
  override val liferayHelper: LiferayHelper = Configuration.liferayHelper
  override val courseService: CourseService = Configuration.courseService

  override def logoFileStorage: FileStorage = Configuration.logoFileStorage

  override def getMimeType(fileName: String): String = MimeTypesUtil.getContentType(fileName)

  override def taskManager: TaskManager = Configuration.taskManager

  override val trainingEventServiceBridge: TrainingEventServiceBridge = Configuration.trainingEventServiceBridge

  override def messageBusService: MessageBusService = Configuration.messageBusService

  override val dbActions: DbActions = Configuration.dbActions

  override protected val webContentService: WebContentService =
    Configuration.webContentService

  override def getPortalURL(request: HttpServletRequest): String = {
    PortalUtil.getPortalURL(request)
  }

  override def getHomePage(companyId: Long): String = Configuration.lrsClientManager.getHomePage(companyId)

  override def valamisContextPath: String = Configuration.valamisContextPath

  override def permissionChecker = new PermissionCheckerImpl

  override def userService: UserService = Configuration.userService

  override val groupService: GroupService = Configuration.groupService

  override def certificateTracker: CertificateTracker = Configuration.certificateTrackerService

  override def certificateNotificationService: CertificateNotificationService =
    Configuration.certificateNotificationService

  override def learningPathService: LearningPathService = Configuration.learningPathService

  override def memberService: LPMemberServiceImpl = Configuration.memberService

  override def lrsClient: LrsClientManager = new LrsClientManagerImpl {
    override def lrsEndpointService: LrsEndpointService = Configuration.lrsEndpointServiceImpl

    override def authCredentials: UserCredentialsStorage = Configuration.authCredentialsImpl

    override def lrsRegistration: LrsRegistration = Configuration.lrsRegistrationImpl

    override def getHost(companyId:Long): String = PortalUtil.getPortalURL(request)
  }
  override def lrsRegistration: LrsRegistration = Configuration.lrsRegistrationImpl
}
