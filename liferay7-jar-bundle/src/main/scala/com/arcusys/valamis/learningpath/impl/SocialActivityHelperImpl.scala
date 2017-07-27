package com.arcusys.valamis.learningpath.impl

import com.arcusys.valamis.learningpath.models.Activity
import com.arcusys.valamis.learningpath.services.SocialActivityHelper
import com.liferay.portal.kernel.service.GroupLocalServiceUtil
import com.liferay.portal.kernel.util.StringPool
import com.liferay.social.kernel.model.{SocialActivity, SocialActivityFeedEntry}
import com.liferay.social.kernel.service.{SocialActivityInterpreterLocalServiceUtil, SocialActivityLocalServiceUtil}
import org.joda.time.DateTime

import scala.util.Random

class SocialActivityHelperImpl[T: Manifest](className: String)
  extends SocialActivityHelper
    with ActivityConverter {

  def this() = {
    this(manifest[T].runtimeClass.getName)
  }

  private lazy val random = new Random

  //Creates activity with activitySet, because activity portlet of social office are retrieved for sets.
  def addWithSet(companyId: Long,
                 userId: Long,
                 courseId: Option[Long] = None,
                 receiverUserId: Option[Long] = None,
                 activityType: Option[Int] = None,
                 classPK: Option[Long] = None,
                 extraData: Option[String] = None,
                 createDate: DateTime): Activity = {

    val groupId = Some(courseId
      .getOrElse(GroupLocalServiceUtil.getGroup(companyId, "Guest").getGroupId))

    val socialActivity =
      create(companyId,
        userId,
        className,
        groupId,
        receiverUserId,
        activityType,
        classPK,
        extraData,
        createDate)

    SocialActivityLocalServiceUtil.addActivity(socialActivity, null)
    toModel(socialActivity)
  }

  private def create(companyId: Long,
                     userId: Long,
                     className: String,
                     courseId: Option[Long],
                     receiverUserId: Option[Long],
                     activityType: Option[Int],
                     classPK: Option[Long],
                     extraData: Option[String],
                     createDate: DateTime) = {
    val socialActivity =  SocialActivityLocalServiceUtil.createSocialActivity(0)

    socialActivity.setCompanyId(companyId)
    socialActivity.setUserId(userId)
    socialActivity.setClassName(className)
    courseId.foreach(socialActivity.setGroupId)
    receiverUserId.foreach(socialActivity.setReceiverUserId)
    activityType.foreach(socialActivity.setType)

    socialActivity.setClassPK(classPK.getOrElse(random.nextLong()))
    // Comments in activity portlet of social office are done toward classPK

    socialActivity.setCreateDate(createDate.getMillis)

    extraData.foreach(socialActivity.setExtraData)

    socialActivity
  }
}