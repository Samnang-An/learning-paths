package com.arcusys.valamis.learningpath.asset

import java.util.Locale
import javax.portlet.{RenderRequest, RenderResponse}

import com.arcusys.valamis.learningpath.models.{LPVersion, LearningPath}
import com.arcusys.valamis.learningpath.strutsactions.LearningPathOpenAction
import com.liferay.portal.kernel.portlet.{LiferayPortletRequest, LiferayPortletResponse}
import com.liferay.portal.kernel.util.{HtmlUtil, WebKeys}
import com.liferay.portal.theme.ThemeDisplay
import com.liferay.portlet.asset.model.{AssetEntry, BaseAssetRenderer}

/**
  * Created by mminin on 01/04/2017.
  */
class LPAssetRenderer(learningPath: LearningPath,
                      version: LPVersion,
                      entry: AssetEntry) extends BaseAssetRenderer {

  override def getGroupId: Long = entry.getGroupId

  override def getUserName: String = entry.getUserName

  override def getUuid: String = "" //TODO: improve some how

  override def getUserId: Long = entry.getUserId

  override def getClassPK: Long = entry.getClassPK

  override def getTitle(locale: Locale): String = version.title

  override def getClassName: String = entry.getClassName

  override def getSummary(locale: Locale): String = {
    HtmlUtil.stripHtml(version.description.getOrElse(version.title))
  }

  override def render(renderRequest: RenderRequest,
                      renderResponse: RenderResponse,
                      template: String): String = {
    throw new NotImplementedError("Learning path render is not supported")
  }

  override def getURLViewInContext(request: LiferayPortletRequest,
                                   response: LiferayPortletResponse,
                                   noSuchEntryRedirect: String): String = {
    val themeDisplay = request.getAttribute(WebKeys.THEME_DISPLAY).asInstanceOf[ThemeDisplay]
    LearningPathOpenAction
      .getURL(themeDisplay.getPlid, learningPath.id, themeDisplay.getPortalURL, maximized = false)
  }

}
