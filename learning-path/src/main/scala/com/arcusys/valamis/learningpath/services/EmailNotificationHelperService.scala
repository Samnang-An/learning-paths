package com.arcusys.valamis.learningpath.services

trait EmailNotificationHelperService {
  def sendNotification(companyId: Long,
                       userId: Long,
                       bodyPreferencesName: String,
                       subjectPreferencesName: String,
                       data: Map[String, String]): Unit

}
