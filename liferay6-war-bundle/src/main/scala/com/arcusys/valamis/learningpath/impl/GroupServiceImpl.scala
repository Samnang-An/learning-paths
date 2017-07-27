package com.arcusys.valamis.learningpath.impl

import com.arcusys.valamis.learningpath.services.GroupService
import com.arcusys.valamis.members.picker.model.MemberTypes
import com.liferay.portal.service.{OrganizationLocalServiceUtil, RoleLocalServiceUtil, UserGroupLocalServiceUtil}

/**
  * Created by pkornilov on 6/1/17.
  */
class GroupServiceImpl extends GroupService {

  override def exists(id: Long, groupType: MemberTypes.Value): Boolean = {
    Option(groupType match {
      case MemberTypes.UserGroup => UserGroupLocalServiceUtil.fetchUserGroup(id)
      case MemberTypes.Role => RoleLocalServiceUtil.fetchRole(id)
      case MemberTypes.Organization => OrganizationLocalServiceUtil.fetchOrganization(id)
      case tpe => throw new IllegalArgumentException(s"Wrong group type: $tpe")
    }).isDefined
  }

}
