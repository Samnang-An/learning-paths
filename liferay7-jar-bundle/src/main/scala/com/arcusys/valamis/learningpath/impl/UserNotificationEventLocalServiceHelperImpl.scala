package com.arcusys.valamis.learningpath.impl


import com.arcusys.valamis.learningpath.services.UserNotificationEventLocalService
import com.liferay.portal.kernel.model.{MembershipRequestConstants, UserNotificationDeliveryConstants}
import com.liferay.portal.kernel.notifications.UserNotificationManagerUtil
import com.liferay.portal.kernel.service.{ServiceContext, ServiceContextThreadLocal, UserNotificationEventLocalServiceUtil}
import org.joda.time.DateTime

class UserNotificationEventLocalServiceHelperImpl extends UserNotificationEventLocalService {

  private def addUserNotificationEvent(userId: Long,
                                       activityType: String,
                                       timestamp: Long,
                                       deliverBy: Long,
                                       payload: String,
                                       archived: Boolean,
                                       serviceContext: ServiceContext): Unit = {
    val classNameId = 0

    if (UserNotificationManagerUtil.isDeliver(userId,
      activityType,
      classNameId,
      MembershipRequestConstants.STATUS_PENDING,
      UserNotificationDeliveryConstants.TYPE_WEBSITE)) {

      UserNotificationEventLocalServiceUtil.addUserNotificationEvent(
        userId,
        activityType,
        timestamp,
        UserNotificationDeliveryConstants.TYPE_WEBSITE,
        deliverBy,
        payload,
        archived,
        serviceContext
      )
    }
  }

  def sendNotification(model: String, userId: Long, achivedType: String): Unit = {
    val serviceContext = Option(ServiceContextThreadLocal.getServiceContext)
      .getOrElse(new ServiceContext())

    addUserNotificationEvent(userId,
      achivedType,
      DateTime.now().getMillis,
      userId,
      model,
      false,
      serviceContext
    )
  }
}
