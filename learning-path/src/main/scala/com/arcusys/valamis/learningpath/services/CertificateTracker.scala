package com.arcusys.valamis.learningpath.services

import com.arcusys.valamis.learningpath.models.{CertificateTrackerFilter, CertificateTrackerModel, LPVersion, UserLPStatus}
import org.joda.time.DateTime

import scala.concurrent.Future

trait CertificateTracker {
  def getExpired(filter: CertificateTrackerFilter)(implicit companyId: Long): Future[CertificateTrackerModel]
  def sendNotification(lpId: Long,
                       userId: Long,
                       expiredDate: DateTime)
                      (implicit companyId: Long): Future[Unit]

  def sendNotifications(trackerFilter: CertificateTrackerFilter)
                       (implicit companyId: Long): Future[Unit]

  def getExpiredDate(status: UserLPStatus,
                     version: LPVersion): Option[DateTime]
}