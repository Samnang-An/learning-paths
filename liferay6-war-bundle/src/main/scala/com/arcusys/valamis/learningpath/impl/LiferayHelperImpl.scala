package com.arcusys.valamis.learningpath.impl

import com.arcusys.valamis.members.picker.model.MemberTypes.MemberType
import com.arcusys.valamis.members.picker.model._
import com.arcusys.valamis.members.picker.model.impl.{LazyGroupInfo, LazyUserInfo}
import com.arcusys.valamis.members.picker.service.LiferayHelper
import com.liferay.portal.kernel.dao.orm.QueryUtil
import com.liferay.portal.kernel.util.{DigesterUtil, HttpUtil}
import com.liferay.portal.kernel.workflow.WorkflowConstants.STATUS_APPROVED
import com.liferay.portal.model._
import com.liferay.portal.security.auth.CompanyThreadLocal
import com.liferay.portal.service.{OrganizationLocalServiceUtil, RoleLocalServiceUtil, UserGroupLocalServiceUtil, UserLocalServiceUtil}
import com.liferay.portal.webserver.WebServerServletTokenUtil

import scala.collection.JavaConverters._

/**
  * Created by mminin on 22/02/2017.
  */
class LiferayHelperImpl extends LiferayHelper {
  override def getCompanyId: Long = CompanyThreadLocal.getCompanyId

  val randomUserCount = 3

  override def getUserInfo(userId: Long, membershipInfo: => Seq[Member]): UserInfo = {
    toUserInfo(UserLocalServiceUtil.getUser(userId), membershipInfo)
  }

  override def getUsers(companyId: Long): Seq[UserInfo] = {
    UserLocalServiceUtil.getCompanyUsers(companyId, QueryUtil.ALL_POS, QueryUtil.ALL_POS).asScala
      .filterNot(u => !u.isActive || u.isDefaultUser)
      .map(toUserInfo(_, Nil))
  }

  override def getUserGroups(companyId: Long): Seq[GroupInfo] = {
    UserGroupLocalServiceUtil.getUserGroups(companyId).asScala map toGroupInfo
  }

  override def getRoles(companyId: Long): Seq[GroupInfo] = {
    RoleLocalServiceUtil.getRoles(companyId).asScala map toGroupInfo
  }

  override def getOrganizations(companyId: Long): Seq[GroupInfo] = {
    OrganizationLocalServiceUtil.getOrganizations(QueryUtil.ALL_POS, QueryUtil.ALL_POS).asScala
      .filter(_.getCompanyId == companyId)
      .map(toGroupInfo)
  }

  override def getGroupsInfo(memberIds: Seq[Long], groupType: MemberType): Seq[GroupInfo] = {
    groupType match {
      case MemberTypes.Role =>
        RoleLocalServiceUtil.getRoles(memberIds.toArray).asScala map toGroupInfo
      case MemberTypes.UserGroup =>
        UserGroupLocalServiceUtil.getUserGroups(memberIds.toArray).asScala map toGroupInfo
      case MemberTypes.Organization =>
        OrganizationLocalServiceUtil.getOrganizations(memberIds.toArray).asScala map toGroupInfo
    }
  }


  override def getMemberUserIds(member: Member): Seq[Long] = {
    member.tpe match {
      case MemberTypes.User => Seq(UserLocalServiceUtil.getUser(member.id).getUserId)
      case MemberTypes.Role => UserLocalServiceUtil.getRoleUserIds(member.id).toSeq
      case MemberTypes.UserGroup => UserLocalServiceUtil.getUserGroupUsers(member.id).asScala map (_.getUserId)
      case MemberTypes.Organization => UserLocalServiceUtil.getOrganizationUserIds(member.id).toSeq
    }
  }

  override protected def getPortraitUrl(userId: Long): String = {
    getPortraitUrl(UserLocalServiceUtil.getUser(userId))
  }

  private def getRoleName(role: Role): String = {
    Option(role.getDescriptiveName) filter (!_.isEmpty) getOrElse role.getName
  }

  private def toUserInfo(user: User, _membershipInfo: => Seq[Member]): UserInfo = {
    LazyUserInfo(id = user.getUserId,
      name = user.getFullName,
      logo = getPortraitUrl(user),
      roles = user.getRoles.asScala.map(role => IdAndName(role.getRoleId, getRoleName(role))),
      groups = user.getUserGroups.asScala.map(group => IdAndName(group.getUserGroupId, group.getName)),
      organizations = user.getOrganizations.asScala.map(org => IdAndName(org.getOrganizationId, org.getName)),
      membershipInfo = _membershipInfo
    )
  }

  private def toGroupInfo(role: Role) =
    LazyGroupInfo(
      role.getRoleId,
      getRoleName(role),
      UserLocalServiceUtil.getRoleUsersCount(role.getRoleId, STATUS_APPROVED),
      getRandomUserPortraits(UserLocalServiceUtil.getRoleUserIds(role.getRoleId).toList, randomUserCount)
    )

  private def toGroupInfo(org: Organization) =
    LazyGroupInfo(
      org.getOrganizationId,
      org.getName,
      UserLocalServiceUtil.getOrganizationUsersCount(org.getOrganizationId, STATUS_APPROVED),
      getRandomUserPortraits(UserLocalServiceUtil.getOrganizationUserIds(org.getOrganizationId).toList, randomUserCount)
    )

  private def toGroupInfo(userGroup: UserGroup) =
    LazyGroupInfo(
      userGroup.getUserGroupId,
      userGroup.getName,
      UserLocalServiceUtil.getUserGroupUsersCount(userGroup.getUserGroupId, STATUS_APPROVED),
      getRandomUserPortraits(
        UserLocalServiceUtil.getUserGroupUsers(userGroup.getUserGroupId, 0, randomUserCount).asScala map(_.getUserId), randomUserCount
      )
    )

  private def getPortraitUrl(user: User): String = {
    val gender = if (user.isMale) "male" else "female"
    val portraitId = user.getPortraitId
    val token = HttpUtil.encodeURL(DigesterUtil.digest(user.getUserUuid))
    val stamp = WebServerServletTokenUtil.getToken(portraitId)

    s"/image/user_${gender}_portrait?img_id=$portraitId&img_id_token=$token&t=$stamp"
  }
}
