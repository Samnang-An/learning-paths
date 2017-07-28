package com.arcusys.valamis.learningpath.services.impl

import com.arcusys.valamis.learningpath.models._
import com.arcusys.valamis.learningpath.services.{CertificateNotificationService, CertificateTracker}
import com.arcusys.valamis.learningpath.utils.{DbActions, DbActionsSupport}
import com.arcusys.valamis.members.picker.model.SkipTake
import com.arcusys.valamis.members.picker.service.LiferayHelper
import org.joda.time.DateTime
import scala.concurrent.{ExecutionContext, Future}

class CertificateTrackerImpl(val dbActions: DbActions,
                             val liferay: LiferayHelper,
                             val certificateNotificationService: CertificateNotificationService)
                            (implicit val executionContext: ExecutionContext)
  extends CertificateTracker
    with DbActionsSupport{

  override def getExpired(trackerFilter: CertificateTrackerFilter)
                         (implicit companyId: Long): Future[CertificateTrackerModel] = {

    val dateNow = DateTime.now()

    val lpWithValidPeriodF = getExpiredWithStatus(trackerFilter: CertificateTrackerFilter)
    val certificateExpiredF = lpWithValidPeriodF.map {
      _.map { case ((lp, version), status) =>
        val expiredDate = getExpiredDate(status, version)
        expiredDate.map { date =>

          val userInfo = liferay.getUserInfo(status.userId, Seq())

          CertificateInfoTrackerModel(
            version.learningPathId,
            version.title,
            date.isAfter(dateNow),
            version.logo.getOrElse(""),
            date,
            status.userId,
            userInfo.name,
            userInfo.logo
          )
        }

      }.map(_.get)
    }

    certificateExpiredF.map { certificateExpired =>
      val totalExpired = certificateExpired.count(!_.certificateExpiredInFuture)
      val totalExpires = certificateExpired.count(_.certificateExpiredInFuture)
      val records = trackerFilter.skipTake match {
        case None => certificateExpired
        case Some(SkipTake(skip, take)) =>
          certificateExpired.slice(skip, skip + take)
      }

      CertificateTrackerModel(records, totalExpired, totalExpires)
    }
  }


  def sendNotifications(trackerFilter: CertificateTrackerFilter)
                       (implicit companyId: Long): Future[Unit] = {
    getExpiredWithStatus(trackerFilter).map {
      _.map { case ((lp, version), status) =>
        val expiredDate = getExpiredDate(status, version)
        expiredDate.map { date =>
          sendNotification(version.learningPathId, status.userId, expiredDate.get)
        }
      }
    }
  }


  override def sendNotification(lpId: Long,
                       userId: Long,
                       expiredDate: DateTime)
                       (implicit companyId: Long): Future[Unit] = {
   val dateNow = DateTime.now
   if (expiredDate.isBefore(dateNow)) {
     certificateNotificationService.sendCertificateExpired(lpId, Seq(userId), expiredDate)
   } else {
     certificateNotificationService.sendCertificateExpires(lpId, Seq(userId), expiredDate)
   }
 }

  private def getExpiredWithStatus(trackerFilter: CertificateTrackerFilter)
                                  (implicit companyId: Long): Future[Seq[((Long, LPVersion), UserLPStatus)]] = {
    val certificateStatuses = db.run(
      versionDBIO.getLearningPathWithValidPeriod(trackerFilter.endDate)(companyId)
    )

    certificateStatuses.map(
      _.filter { case ((lp, version), status) =>
        status.status match {
          case CertificateStatuses.Success =>
            getExpiredDate(status, version)
              .exists(date =>
                date.isAfter(trackerFilter.startDate.withTimeAtStartOfDay) &&
                  date.isBefore(trackerFilter.endDate)
              )
          case CertificateStatuses.Overdue =>
            status.modifiedDate.isAfter(trackerFilter.startDate.withTimeAtStartOfDay()) &&
              status.modifiedDate.isBefore(trackerFilter.endDate)
          case _ => false
        }
      }
    )
  }

  override def getExpiredDate(status: UserLPStatus,
                             version: LPVersion): Option[DateTime] = {
    status.status match {
      case CertificateStatuses.Overdue =>
        Some(status.modifiedDate)
      case CertificateStatuses.Success =>
        Some(status.modifiedDate.plus(version.validPeriod.get))
      case _ => None
    }
  }
}
