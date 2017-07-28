package com.arcusys.valamis.learningpath.models

import org.joda.time.DateTime

case class CertificateInfoTrackerModel(
   certificateId: Long,
   certificateTitle: String,
   certificateExpiredInFuture: Boolean,
   certificateLogo: String,
   expiredDate: DateTime,
   userId: Long,
   userName: String,
   userLogo: String)

case class CertificateTrackerModel(certificatesInfo: Seq[CertificateInfoTrackerModel],
                                   totalExpired: Long,
                                   totalExpires: Long)