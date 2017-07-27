package com.arcusys.valamis.learningpath.init.notification

import java.util.Locale

import com.arcusys.valamis.learningpath.init.Configuration
import com.liferay.portal.kernel.language.LanguageUtil
import com.liferay.portal.kernel.notifications.{UserNotificationDefinition, UserNotificationDeliveryType}

class CertificateNotificationDefinition
  extends UserNotificationDefinition(Configuration.LearningPathPortletId,
    0L,
    0,
    "notification.certificate") {

    this.addUserNotificationDeliveryType(new UserNotificationDeliveryType("website", 10002, true, true))

    override def getDescription(locale: Locale): String = LanguageUtil.get(Locale.getDefault,
        "notification.certificate")
}
