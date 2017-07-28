package com.arcusys.valamis.learningpath


import com.arcusys.valamis.learningpath.services.CertificateNotificationService
import org.joda.time.DateTime

import scala.concurrent.Future

class CertificateNotificationServiceTestImpl extends CertificateNotificationService {
  override def sendCertificateExpires(lpId: Long,
                                      userId: Seq[Long],
                                      expiresDate: DateTime)(implicit companyId: Long): Future[Unit] =
    Future.successful(Unit)

  override def sendCertificateExpired(lpId: Long,
                                      userId: Seq[Long],
                                      expiresDate: DateTime)(implicit companyId: Long): Future[Unit] =
    Future.successful(Unit)

  override def sendUsersAddedNotification(lpId: Long, userId: Seq[Long])(implicit companyId: Long): Future[Unit] =
    Future.successful(Unit)

  override def sendCertificateDeactivated(lpId: Long, userId: Seq[Long])(implicit companyId: Long): Future[Unit] =
    Future.successful(Unit)

  override def sendCertificateAchieved(lpId: Long, userIds: Seq[Long])(implicit companyId: Long): Future[Unit] =
    Future.successful(Unit)
}
