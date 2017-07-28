package com.arcusys.valamis.learningpath.services

import org.joda.time.DateTime

import scala.concurrent.Future


trait CertificateNotificationService {
  def sendCertificateAchieved(lpId: Long,
                              userIds: Seq[Long])
                             (implicit companyId: Long): Future[Unit]

  def sendCertificateExpires(lpId: Long,
                             userIds: Seq[Long],
                             expiresDate: DateTime)
                            (implicit companyId: Long): Future[Unit]

  def sendCertificateExpired(lpId: Long,
                             userIds: Seq[Long],
                             expiresDate: DateTime)
                            (implicit companyId: Long): Future[Unit]

  def sendUsersAddedNotification(lpId: Long,
                                 userIds: Seq[Long])(implicit companyId: Long): Future[Unit]

  def sendCertificateDeactivated(lpId: Long,
                                 userId: Seq[Long])(implicit companyId: Long): Future[Unit]


}