package com.arcusys.valamis.learningpath.handler

import com.arcusys.json.JsonHelper
import com.arcusys.valamis.learningpath.Configuration
import com.arcusys.valamis.learningpath.models.CertificateNotificationModel
import com.liferay.portal.kernel.language.LanguageUtil
import com.liferay.portal.kernel.notifications.BaseUserNotificationHandler
import com.liferay.portal.model.UserNotificationEvent
import com.liferay.portal.service.{ServiceContext, UserLocalServiceUtil}


class CertificateNotificationHandler extends BaseUserNotificationHandler {
  setPortletId(Configuration.LearningPathPortletId)

  override protected def getLink(userNotificationEvent: UserNotificationEvent, serviceContext: ServiceContext) = {
    val notification = JsonHelper.fromJson[CertificateNotificationModel](userNotificationEvent.getPayload)
    notification.certificateLink

  }

  override protected def getBody(userNotificationEvent: UserNotificationEvent, serviceContext: ServiceContext) = {
    val notification = JsonHelper.fromJson[CertificateNotificationModel](userNotificationEvent.getPayload)

    val userLocale = UserLocalServiceUtil.getUser(notification.userId).getLocale
    val tpl = LanguageUtil.get(userLocale, s"certificate.${notification.messageType}")

    getParams(tpl, notification)
  }

  private def getParams(tpl: String, notification: CertificateNotificationModel): String = {
    tpl.replace("{{title}}", notification.certificateTitle)

  }
}
