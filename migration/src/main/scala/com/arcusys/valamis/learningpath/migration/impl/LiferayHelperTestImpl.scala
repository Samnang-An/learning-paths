package com.arcusys.valamis.learningpath.migration.impl

import com.arcusys.valamis.members.picker.model.MemberTypes.MemberType
import com.arcusys.valamis.members.picker.model._
import com.arcusys.valamis.members.picker.model.impl.{ForcedGroupInfo, ForcedUserInfo}
import com.arcusys.valamis.members.picker.service.LiferayHelper

import scala.util.Try

class LiferayHelperTestImpl(users: Seq[ForcedUserInfo] = Nil,
                            roles: Seq[IdAndName] = Nil,
                            organizations: Seq[IdAndName] = Nil,
                            userGroups: Seq[IdAndName] = Nil)
  extends LiferayHelper {


  override def getCompanyId: Long = {
    ???
  }

  override protected def getPortraitUrl(userId: Long): String = s"users/$userId/logo"


  override def getRoles(companyId: Long): Seq[GroupInfo] = {
    roles.map {
      r => ForcedGroupInfo(r.id, r.name, users.count(_.roles.contains(r)), Nil)
    }
  }

  override def getOrganizations(companyId: Long): Seq[GroupInfo] = {
    organizations.map { o =>
      ForcedGroupInfo(o.id, o.name, users.count(_.organizations.contains(o)), Nil)
    }
  }

  override def getUserGroups(companyId: Long): Seq[GroupInfo] = {
    userGroups.map { g =>
      ForcedGroupInfo(g.id, g.name, users.count(_.groups.contains(g)), Nil)
    }
  }

  override def getUsers(companyId: Long): Seq[UserInfo] = users


  override def getMemberUserIds(member: Member): Seq[Long] = {
    member.tpe match {
      case MemberTypes.User => users.find(_.id == member.id).map(_.id).toSeq
      case MemberTypes.Role => users.filter(_.roles.exists(r => r.id == member.id)).map(_.id)
      case MemberTypes.UserGroup =>
        users.filter(_.groups.exists(r => r.id == member.id)).map(_.id)
      case MemberTypes.Organization =>
        users.filter(_.organizations.exists(r => r.id == member.id)).map(_.id)
    }
  }

  override def getUserInfo(userId: Long, membershipInfo: => Seq[Member]): UserInfo = {
    users
      .find(_.id == userId)
      .getOrElse(???)
      .copy(membershipInfo = membershipInfo)
  }

  override def getGroupsInfo(memberIds: Seq[Long], tpe: MemberType): Seq[GroupInfo] = {
    tpe match {
      case MemberTypes.User => ??? //unsupported
      case MemberTypes.Role =>
        getRoles(getCompanyId).filter(r => memberIds.contains(r.id))
      case MemberTypes.UserGroup =>
        getUserGroups(getCompanyId).filter(g => memberIds.contains(g.id))
      case MemberTypes.Organization =>
        getOrganizations(getCompanyId).filter(o => memberIds.contains(o.id))
    }
  }

}
