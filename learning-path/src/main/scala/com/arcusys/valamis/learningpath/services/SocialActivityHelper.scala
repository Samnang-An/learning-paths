package com.arcusys.valamis.learningpath.services


import com.arcusys.valamis.learningpath.models.Activity
import org.joda.time.DateTime

trait SocialActivityHelper {

    def addWithSet(companyId: Long,
                   userId: Long,
                   courseId: Option[Long] = None,
                   receiverUserId: Option[Long] = None,
                   activityType: Option[Int] = None,
                   classPK: Option[Long] = None,
                   extraData: Option[String] = None,
                   createDate: DateTime): Activity
}