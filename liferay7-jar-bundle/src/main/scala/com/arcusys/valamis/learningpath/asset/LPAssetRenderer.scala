package com.arcusys.valamis.learningpath.asset

import java.util.Locale
import javax.portlet.{PortletRequest, PortletResponse}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.arcusys.valamis.learningpath.models.{LPVersion, LearningPath, LearningPathWithVersion}
import com.arcusys.valamis.learningpath.strutsactions.LearningPathOpenAction
import com.liferay.asset.kernel.model.{AssetEntry, BaseAssetRenderer}
import com.liferay.portal.kernel.portlet.{LiferayPortletRequest, LiferayPortletResponse}
import com.liferay.portal.kernel.theme.ThemeDisplay
import com.liferay.portal.kernel.util.{HtmlUtil, WebKeys}

/**
  * Created by mminin on 31/03/2017.
  */
class LPAssetRenderer(learningPath: LearningPath,
                      version: LPVersion,
                      entry: AssetEntry) extends BaseAssetRenderer[LearningPathWithVersion] {

  override def getGroupId: Long = entry.getGroupId

  override def getUserName: String = entry.getUserName

  override def getUuid: String = "" //TODO: improve some how

  override def getUserId: Long = entry.getUserId

  override def getClassPK: Long = entry.getClassPK

  override def getTitle(locale: Locale): String = version.title

  override def getClassName: String = entry.getClassName

  override def getSummary(portletRequest: PortletRequest,
                          portletResponse: PortletResponse): String = {
    HtmlUtil.stripHtml(version.description.getOrElse(version.title))
  }

  override def getAssetObject: LearningPathWithVersion = {
    LearningPathWithVersion(learningPath, version)
  }

  override def include(request: HttpServletRequest,
                       response: HttpServletResponse,
                       template: String): Boolean = {
    //it is about include jsp
    //we do not need it at all
    false
  }

  override def getURLViewInContext(request: LiferayPortletRequest,
                                   response: LiferayPortletResponse,
                                   noSuchEntryRedirect: String): String = {
    val themeDisplay = request.getAttribute(WebKeys.THEME_DISPLAY).asInstanceOf[ThemeDisplay]
    LearningPathOpenAction
      .getURL(themeDisplay.getPlid, learningPath.id, themeDisplay.getPortalURL, maximized = false)
  }

}
