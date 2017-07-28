package com.arcusys.valamis.learningpath.services

trait UserNotificationEventLocalService {
  
  def sendNotification(model: String, userId: Long, achivedType: String): Unit

}
