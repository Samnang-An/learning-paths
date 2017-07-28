package com.arcusys.valamis.learningpath.listeners

import com.arcusys.valamis.learningpath.Configuration
import com.arcusys.valamis.learningpath.utils.LiferayLogSupport
import com.arcusys.valamis.members.picker.model.MemberTypes
import com.liferay.portal.model._
import com.liferay.portal.security.auth.CompanyThreadLocal
import com.liferay.portal.service.CompanyLocalServiceUtil

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created by mminin on 28/02/2017.
  */
class UserListener extends BaseModelListener[User] with LiferayLogSupport {


  private val classNameToGroupType = Map(
    classOf[Organization].getCanonicalName -> MemberTypes.Organization,
    classOf[UserGroup].getCanonicalName -> MemberTypes.UserGroup,
    classOf[Role].getCanonicalName -> MemberTypes.Role
  )

  override def onAfterRemove(user: User): Unit = await {
    Configuration.memberListener.onUserRemoved(user.getUserId)
  }

  override def onAfterAddAssociation(userPK: scala.Any,
                                     groupClassName: String,
                                     groupPK: scala.Any): Unit = {
    updateAssociation(userPK, groupClassName, groupPK, Configuration.memberListener.onUserJoinGroup)
  }


  override def onAfterRemoveAssociation(userPK: scala.Any,
                                        groupClassName: String,
                                        groupPK: scala.Any): Unit = {
    updateAssociation(userPK, groupClassName, groupPK, Configuration.memberListener.onUserLeaveGroup)
  }

  private def updateAssociation(userPK: scala.Any,
                                groupClassName: String,
                                groupPK: scala.Any,
                                action: (Long, Long, MemberTypes.Value) => Future[_]
                               ): Unit = {

    classNameToGroupType.get(groupClassName).foreach { groupType =>
      (userPK, groupPK) match {
        case (userId: Long, groupId: Long) =>
          CompanyThreadLocal.setCompanyId(CompanyLocalServiceUtil.getCompanyIdByUserId(userId))
          await {
            action(userId, groupId, groupType)
          }
        case _ =>
          log.error(s"Wrong type of user and/or group id! userId: $userPK, groupId: $groupPK")
      }
    }
  }

  private def await(f: Future[_]): Unit = {
    Await.ready(f, Duration.Inf).value.get
      .recover { case e: Throwable => log.error("User listener error", e) }
  }
}
