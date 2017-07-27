package com.arcusys.valamis.learningpath.impl

import javax.mail.internet.InternetAddress

import com.arcusys.valamis.learningpath.services.EmailNotificationHelperService
import com.liferay.mail.kernel.model.MailMessage
import com.liferay.mail.kernel.service.MailServiceUtil
import com.liferay.portal.kernel.model.User
import com.liferay.portal.kernel.service.UserLocalServiceUtil
import com.liferay.portal.kernel.util._

class EmailNotificationHelperImpl extends EmailNotificationHelperService {

  def sendNotification(companyId: Long,
                       userId: Long,
                       bodyPreferencesName: String,
                       subjectPreferencesName: String,
                       data: Map[String, String]): Unit = {


    val user = UserLocalServiceUtil.fetchUser(userId)

    val template = getTemplate(companyId, bodyPreferencesName, subjectPreferencesName, user)


    val currentBody = StringUtil.replace(template._2, data.keys.toArray, data.values.toArray)

    sendEmailNotification(companyId, user, template._1, currentBody)

  }

  private def getTemplate(companyId: Long,
                          bodyPreferencesName: String,
                          subjectPreferencesName: String,
                          user: User): (String, String) = {
    val companyPortletPreferences = PrefsPropsUtil.getPreferences(companyId, true)
    val body = LocalizationUtil.getPreferencesValue(companyPortletPreferences,
      bodyPreferencesName,
      user.getLanguageId)
    val subject = LocalizationUtil.getPreferencesValue(companyPortletPreferences,
      subjectPreferencesName,
      user.getLanguageId)
    (subject, body)
  }

  private def sendEmailNotification(companyId: Long,
                                    user: User,
                                    subject: String,
                                    body: String) = {

    val adminEmail = PrefsPropsUtil.getString(companyId, PropsKeys.ADMIN_EMAIL_FROM_ADDRESS)
    val userEmail = user.getEmailAddress
    val mail = new MailMessage

    mail.setTo(new InternetAddress(userEmail))
    mail.setFrom(new InternetAddress(adminEmail))
    mail.setSubject(subject)
    mail.setBody(body)
    mail.setHTMLFormat(true)


    MailServiceUtil.sendEmail(mail)
  }
}
