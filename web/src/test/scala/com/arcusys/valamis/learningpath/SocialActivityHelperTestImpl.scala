package com.arcusys.valamis.learningpath

import com.arcusys.valamis.learningpath.models.Activity
import com.arcusys.valamis.learningpath.services.SocialActivityHelper
import org.joda.time.DateTime


class SocialActivityHelperTestImpl extends SocialActivityHelper {

  override def addWithSet(companyId: Long,
                          userId: Long,
                          courseId: Option[Long],
                          receiverUserId: Option[Long],
                          activityType: Option[Int],
                          classPK: Option[Long],
                          extraData: Option[String],
                          createDate: DateTime): Activity =
    Activity(1, 1, "className", 1, DateTime.now, 1, None, None, None)
}
