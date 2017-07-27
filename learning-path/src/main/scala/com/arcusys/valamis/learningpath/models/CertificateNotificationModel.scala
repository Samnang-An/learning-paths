package com.arcusys.valamis.learningpath.models

case class CertificateNotificationModel(messageType: String,
                                         certificateTitle: String,
                                         certificateLink: String,
                                         userId: Long)
