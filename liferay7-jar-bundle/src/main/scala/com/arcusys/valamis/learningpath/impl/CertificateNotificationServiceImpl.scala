package com.arcusys.valamis.learningpath.impl

import com.arcusys.json.JsonHelper
import com.arcusys.valamis.learningpath.init.Configuration
import com.arcusys.valamis.learningpath.models.{CertificateNotificationModel, LPVersion, LearningPath}
import com.arcusys.valamis.learningpath.services.{CertificateNotificationService, EmailNotificationHelperService, UserNotificationEventLocalService}
import com.liferay.portal.kernel.log.{Log, LogFactoryUtil}
import com.liferay.portal.kernel.model.User
import com.liferay.portal.kernel.service.{CompanyLocalServiceUtil, UserLocalServiceUtil}
import com.liferay.portal.kernel.util.{PortalUtil, PrefsPropsUtil}
import org.joda.time.{DateTime, Days}

import scala.concurrent.{ExecutionContext, Future}


class CertificateNotificationServiceImpl(val emailNotificationHelper: EmailNotificationHelperService,
                                         val userNotificationEventHelper: UserNotificationEventLocalService)
                                        (implicit val executionContext: ExecutionContext)
  extends CertificateNotificationService {
  private val dateTimePattern = "yyyy-MM-dd"
  private val log: Log = LogFactoryUtil.getLog(this.getClass)

  def sendCertificateAchieved(lpId: Long,
                             userIds: Seq[Long])(implicit companyId: Long): Future[Unit] = {
    try {
      Configuration.learningPathService.getById(lpId)(companyId).map {
        case None =>
        case Some((lp, lpVersion)) =>
          userIds.foreach {
            sendAchievedNotification(_, lp, lpVersion)
          }
      } recover {
        case e: Exception => log.error(e)
      }
    } catch {
      case e: Exception => log.error(e)
        Future.successful(Unit)
    }
  }

  private def sendAchievedNotification(userId: Long, lp: LearningPath, lpVersion: LPVersion) = {
    val user = UserLocalServiceUtil.getUser(userId)
    val link = getUrl(lpVersion.learningPathId, lp.companyId)
    if (PrefsPropsUtil.getBoolean(lp.companyId, "valamis.certificate.user.achieved.enable")) {
      val linkHtml = getLink(link, lpVersion.title)
      val company = CompanyLocalServiceUtil.getCompany(lp.companyId)
      emailNotificationHelper.sendNotification(lp.companyId,
        userId,
        "valamisCertificateUserAchievedBody",
        "valamisCertificateUserAchievedSubject",
        Map(
          "[$CERTIFICATE_NAME$]" -> lpVersion.title,
          "[$CERTIFICATE_PRINT_LINK$]" -> linkHtml, // TODO change for pdf link
          "[$USER_SCREENNAME$]" -> user.getFullName,
          "[$PORTAL_URL$]" -> company.getVirtualHostname
        )
      )
    }
    prepareSendNotification(user.getUserId,
      "achieved",
      lpVersion.title,
      link)
  }


  def sendCertificateExpires(lpId: Long,
                             userIds: Seq[Long],
                             date: DateTime)(implicit companyId: Long): Future[Unit] = {
    try {
      Configuration.learningPathService.getById(lpId)(companyId).map {
        case None =>
        case Some((lp, lpVersion)) =>
          userIds.foreach {
            sendExpiresNotification(_, date, lp, lpVersion)
          }
      } recover {
        case e: Exception => log.error(e)
      }
    } catch {
      case e: Exception => log.error(e)
        Future.successful(Unit)
    }
  }


  private def sendExpiresNotification(userId: Long, date: DateTime, lp: LearningPath, lpVersion: LPVersion) = {
    val user = UserLocalServiceUtil.getUser(userId)
    val days = Days.daysBetween(DateTime.now.withTimeAtStartOfDay(), date.withTimeAtStartOfDay()).getDays()
    val link = getUrl(lpVersion.learningPathId, lp.companyId)
    if (PrefsPropsUtil.getBoolean(lp.companyId, "valamis.certificate.expires.enable")) {
      val linkHtml = getLink(link, lpVersion.title)
      val company = CompanyLocalServiceUtil.getCompany(lp.companyId)
      emailNotificationHelper.sendNotification(lp.companyId,
        user.getUserId,
        "valamisCertificateExpiresBody",
        "valamisCertificateExpiresSubject",
        Map(
          "[$CERTIFICATE_LINK$]" -> linkHtml,
          "[$DAYS$]" -> days.toString,
          "[$DATE$]" -> date.toString(dateTimePattern),
          "[$USER_SCREENNAME$]" -> user.getFullName,
          "[$PORTAL_URL$]" -> company.getVirtualHostname
        )
      )
    }
    prepareSendNotification(user.getUserId,
      "expires",
      lpVersion.title,
      link)
  }

  def sendCertificateExpired(lpId: Long,
                             userIds: Seq[Long],
                             date: DateTime)(implicit companyId: Long): Future[Unit] = {
    try {
      Configuration.learningPathService.getById(lpId)(companyId).map {
        case None =>
        case Some((lp, lpVersion)) =>
          userIds.foreach {
            sendExpiredNotification(_, date, lp, lpVersion)
          }
      } recover {
        case e: Exception => log.error(e)
      }
    } catch {
      case e: Exception => log.error(e)
        Future.successful(Unit)
    }
  }

  private def sendExpiredNotification(userId: Long, date: DateTime, lp: LearningPath, lpVersion: LPVersion) = {
    val user = UserLocalServiceUtil.getUser(userId)
    val days = Days.daysBetween(date.withTimeAtStartOfDay(), DateTime.now.withTimeAtStartOfDay()).getDays()
    val link = getUrl(lpVersion.learningPathId, lp.companyId)
    if (PrefsPropsUtil.getBoolean(lp.companyId, "valamis.certificate.expired.enable")) {
      val linkHtml = getLink(link, lpVersion.title)
      val company = CompanyLocalServiceUtil.getCompany(lp.companyId)
      emailNotificationHelper.sendNotification(lp.companyId,
        user.getUserId,
        "valamisCertificateExpiredBody",
        "valamisCertificateExpiredSubject",
        Map(
          "[$CERTIFICATE_LINK$]" -> linkHtml,
          "[$DAYS$]" -> days.toString,
          "[$DATE$]" -> date.toString(dateTimePattern),
          "[$USER_SCREENNAME$]" -> user.getFullName,
          "[$PORTAL_URL$]" -> company.getVirtualHostname
        )
      )
    }

    prepareSendNotification(user.getUserId,
      "expired",
      lpVersion.title,
      link)
  }

  def sendUsersAddedNotification(lpId: Long,
                                 userIds: Seq[Long])(implicit companyId: Long): Future[Unit] = {
    try {

      Configuration.learningPathService.getById(lpId)(companyId).map {
        case None =>
        case Some((lp, lpVersion)) =>
          userIds.foreach { sendUserAddedNotification(lp, lpVersion, _) }
      } recover {
        case e: Exception => log.error(e)
      }
    } catch {
      case e: Exception => log.error(e)
        Future.successful(Unit)
    }
  }

  private def sendUserAddedNotification(lp: LearningPath, lpVersion: LPVersion, userId: Long) = {
    val user = UserLocalServiceUtil.getUser(userId)

    val link = getUrl(lpVersion.learningPathId, lp.companyId)
    if (PrefsPropsUtil.getBoolean(lp.companyId, "valamis.certificate.user.added.enable")) {
      val linkHtml = getLink(link, lpVersion.title)
      val company = CompanyLocalServiceUtil.getCompany(lp.companyId)
      emailNotificationHelper.sendNotification(lp.companyId,
        user.getUserId,
        "valamisCertificateUserAddedBody",
        "valamisCertificateUserAddedSubject",
        Map(
          "[$CERTIFICATE_NAME$]" -> lpVersion.title,
          "[$CERTIFICATE_LINK$]" -> linkHtml,
          "[$USER_SCREENNAME$]" -> user.getFullName,
          "[$PORTAL_URL$]" -> company.getVirtualHostname

        )
      )
    }
    prepareSendNotification(
      user.getUserId,
      "added",
      lpVersion.title,
      link
    )
  }

  def sendCertificateDeactivated(lpId: Long,
                                 userIds: Seq[Long])(implicit companyId: Long): Future[Unit] = {

    try {

      Configuration.learningPathService.getById(lpId)(companyId).map {
        case None =>
        case Some((lp, lpVersion)) =>
          userIds.foreach { sendDeactivatedNotification(_, lp, lpVersion) }
      } recover {
        case e: Exception => log.error(e)
      }
    } catch {
      case e: Exception => log.error(e)
        Future.successful(Unit)
    }

  }

  private def sendDeactivatedNotification(userId: Long, lp: LearningPath, lpVersion: LPVersion) = {
    val user = UserLocalServiceUtil.getUser(userId)
    val link = getUrl(lpVersion.learningPathId, lp.companyId)
    if (PrefsPropsUtil.getBoolean(lp.companyId, "valamis.certificate.user.deactivated.enable")) {
      val linkHtml = getLink(link, lpVersion.title)
      val company = CompanyLocalServiceUtil.getCompany(lp.companyId)
      emailNotificationHelper.sendNotification(lp.companyId,
        user.getUserId,
        "valamisCertificateUserDeactivatedBody",
        "valamisCertificateUserDeactivatedSubject",
        Map(
          "[$CERTIFICATE_NAME$]" -> lpVersion.title,
          "[$USER_SCREENNAME$]" -> user.getFullName,
          "[$PORTAL_URL$]" -> company.getVirtualHostname
        )
      )
    }
    prepareSendNotification(user.getUserId,
      "deactivated",
      lpVersion.title,
      link)
  }

  private def prepareSendNotification(userId: Long, messageType: String, title: String, link: String): Unit = {
    val model = CertificateNotificationModel(messageType, title, link, userId)
    userNotificationEventHelper.sendNotification(JsonHelper.toJson(model),
      userId,
      Configuration.LearningPathPortletId)
  }

  private def getLink(link: String, name: String): String = "<a href=\"" + link + "\">" + name + "</a>"


  private def getUrl(lpId: Long, companyId: Long, isSecure : Boolean = false): String ={
    lazy val company = CompanyLocalServiceUtil.getCompany(companyId)

    val hostName = company.getVirtualHostname
    val port = PortalUtil.getPortalServerPort(isSecure)
    val link = PortalUtil.getPortalURL(hostName, port, isSecure) + PortalUtil.getPathContext
    link + "/c/portal/learning-path/open?lpId=" + lpId
  }
}