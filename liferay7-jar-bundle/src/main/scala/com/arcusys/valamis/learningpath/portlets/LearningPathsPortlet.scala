package com.arcusys.valamis.learningpath.portlets

import java.io.FileNotFoundException
import java.util.Locale
import javax.portlet._
import javax.servlet.http.HttpServletRequest

import com.arcusys.valamis.learningpath.init.Configuration
import com.arcusys.valamis.learningpath.listeners.WebContentListener
import com.arcusys.valamis.learningpath.services.{AssignmentSupport, LessonService, MessageBusService, TrainingEventServiceBridge}
import com.arcusys.valamis.lrssupport.lrs.service.{LrsOAuthService, LrsRegistration, LrsRegistrationImpl, UserCredentialsStorage}
import com.arcusys.valamis.lrssupport.lrsEndpoint.service.LrsEndpointService
import com.arcusys.valamis.lrssupport.lrsEndpoint.storage.LrsTokenStorage
import com.arcusys.valamis.lrssupport.oauth.OAuthPortlet
import com.liferay.journal.service.JournalArticleLocalServiceUtil
import com.liferay.portal.kernel.portlet.{PortletRequestModelFactory, PortletURLUtil}
import com.liferay.portal.kernel.service.permission.PortletPermissionUtil
import com.liferay.portal.kernel.theme.ThemeDisplay
import com.liferay.portal.kernel.util.{PortalUtil, WebKeys}

import scala.language.postfixOps

/**
  * Created by mminin on 08/02/2017.
  */
class LearningPathsPortlet
  extends OAuthPortlet
    with AssignmentSupport
    with I18nSupport {

  override lazy val messageBusService: MessageBusService = Configuration.messageBusService
  lazy val trainingEventServiceBridge: TrainingEventServiceBridge = Configuration.trainingEventServiceBridge
  lazy val lessonService: LessonService = Configuration.lessonService
  lazy val webContentListener: WebContentListener = Configuration.webContentListener

  override def doView(request: RenderRequest, response: RenderResponse): Unit = {
    val contextPath = PortalUtil.getPathContext(request)

    val themeDisplay = request.getAttribute(WebKeys.THEME_DISPLAY).asInstanceOf[ThemeDisplay]
    val groupId = themeDisplay.getScopeGroupId
    val permissionChecker = themeDisplay.getPermissionChecker
    val portletId = PortalUtil.getPortletId(request)
    val primaryKey = PortletPermissionUtil.getPrimaryKey(themeDisplay.getPlid, portletId)

    val permissionToModify = permissionChecker
      .hasPermission(groupId, portletId, primaryKey, "MODIFY_ACTION")

    val subContext = "learning-paths"
    val root = getRoot(request, response)
    val actionUrl = scala.xml.Unparsed(response.createResourceURL().toString)

    val content =
      <div>
        <div id="learningPathsAppRegion" class="val-portlet">
        </div>
        <script type="text/javascript">
          var learningPathsAppOptions = {{
            'root': '{root}',
            'apiPath': '/delegate/',
            'resourcePath': '{contextPath}/',
            'endpoint': {{
              'learningPaths': '{subContext}/learning-paths/',
              'versions': '{subContext}/versions/',
              'users': '{subContext}/users/',
              'goals': '{subContext}/goals/',
              'goalGroups': '{subContext}/goal-groups/',
              'liferayActivities': '{subContext}/lr-activity-types/',
              'courses': '{subContext}/courses/',
              'assignments': 'assignments/',
              'lessons': 'packages/',
              'lessonsPublic': 'lessons/',
              'webContents': '{subContext}/web-contents/',
              'trainingEvents': 'training-events/',
              'sequencing': 'sequencing/Tincan/',
              'statements': 'statements/',
              'lrsSettings': '{subContext}/lrs-settings/',
              'expiredCertificates': '{subContext}/expired-certificates/',
              'actionUrl': '{actionUrl}',
              'skills': 'competences/skills',
              'levels': 'competences/levels'
            }},
            'permissions': {{
              'modify': {permissionToModify}
            }},
            'isAssignmentDeployed': {isAssignmentDeployed},
            'isTrainingEventDeployed': {trainingEventServiceBridge.isTrainingEventsDeployed(companyId)},
            'isValamisDeployed': {lessonService.isValamisDeployed}
          }};
          require('learning-paths', function (module) {{
            module.default.start();
          }})
        </script>
      </div>

    val out = response.getWriter
    out.write(content.toString())
  }

  private def getRoot(request: RenderRequest, response: RenderResponse) = {
    val url = PortletURLUtil.getCurrent(request, response)
    val parts = url.toString.split("/")
    if (parts.length > 2) parts.tail.tail.head else ""
  }

  protected def getTranslation(view: String, language: String): Map[String, String] = {
    try {
      getTranslation("/i18n/" + view + "_" + language)
    } catch {
      case e: FileNotFoundException => getTranslation("/i18n/" + view + "_en")
      case _: Throwable => Map[String, String]()
    }
  }

  override def serveResource(request: ResourceRequest, response: ResourceResponse) {

    val resourceType = request.getParameter("resourceType")
    resourceType match {
      case "web-content" => getWebContent(request, response)
      case _ => super.serveResource(request, response)
    }
  }

  private def getWebContent(request: ResourceRequest, response: ResourceResponse) = {
    val articleId = request.getParameter("articleID").toLong
    val articleLanguage = request.getParameter("language")
    val td = request.getAttribute(WebKeys.THEME_DISPLAY).asInstanceOf[ThemeDisplay]

    val portletRequestFactory = new PortletRequestModelFactory(request, response)
    val portletRequestModel = portletRequestFactory.getPortletRequestModel

    val article = Option(JournalArticleLocalServiceUtil.fetchJournalArticle(articleId))
    val text = Option(JournalArticleLocalServiceUtil.fetchJournalArticle(articleId))
      .map { article =>
        val latest = JournalArticleLocalServiceUtil.getLatestArticle(article.getGroupId, article.getArticleId)
        val articleDisplay = JournalArticleLocalServiceUtil.getArticleDisplay(
          latest.getGroupId,
          latest.getArticleId,
          "view",
          articleLanguage,
          td)

        JournalArticleLocalServiceUtil.getArticleContent(
          latest,
          articleDisplay.getDDMTemplateKey,
          "view",
          articleLanguage,
          portletRequestModel,
          td)
      }

    if (article.isDefined) {
      webContentListener.onWebContentViewed(td.getUserId,
        articleId)(td.getCompanyId)
    }
    response.getWriter.println(text.getOrElse("Not supported content"))
  }

  override protected def getHttpRequest(renderRequest: RenderRequest): HttpServletRequest =
    PortalUtil.getOriginalServletRequest(PortalUtil.getHttpServletRequest(renderRequest))

  override implicit def companyId: Long = Configuration.liferayHelper.getCompanyId


  override protected def lrsRegistration: LrsRegistration = new LrsRegistrationImpl {
    override def lrsEndpointService: LrsEndpointService = Configuration.lrsEndpointServiceImpl

    override def lrsOAuthService: LrsOAuthService = Configuration.lrsOAuthServiceImpl

    override def lrsTokenStorage: LrsTokenStorage = Configuration.lrsTokenStorageImpl
  }

  override protected def authCredentials: UserCredentialsStorage = Configuration.authCredentialsImpl
}
