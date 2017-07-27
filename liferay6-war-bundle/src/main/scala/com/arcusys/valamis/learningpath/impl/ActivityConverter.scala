package com.arcusys.valamis.learningpath.impl

import com.arcusys.valamis.learningpath.models.Activity
import com.liferay.portal.kernel.util.StringPool
import com.liferay.portal.service.ServiceContextThreadLocal
import com.liferay.portlet.social.model.SocialActivity
import com.liferay.portlet.social.service.SocialActivityInterpreterLocalServiceUtil
import org.joda.time.DateTime

trait ActivityConverter {


  private def toOption(liferayOptionalValue: Long) = {
    Some(liferayOptionalValue).filterNot(_ == 0)
  }

  private def toOption(liferayOptionalValue: String) = {
    Some(liferayOptionalValue).filterNot(_.isEmpty)
  }

  protected def toModel(from: SocialActivity): Activity = {
    Activity(
      id = from.getActivityId,
      userId = from.getUserId,
      className = from.getClassName,
      companyId = from.getCompanyId,
      createDate = new DateTime(from.getCreateDate),
      activityType = from.getType,
      classPK = toOption(from.getClassPK),
      groupId = toOption(from.getGroupId),
      extraData = toOption(from.getExtraData)
    )
  }
}