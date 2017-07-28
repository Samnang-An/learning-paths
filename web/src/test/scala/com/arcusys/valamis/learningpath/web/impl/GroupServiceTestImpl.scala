package com.arcusys.valamis.learningpath.web.impl

import com.arcusys.valamis.learningpath.services.GroupService
import com.arcusys.valamis.members.picker.model.MemberTypes

/**
  * Created by pkornilov on 6/2/17.
  */
class GroupServiceTestImpl(groupIds: Map[MemberTypes.Value, Seq[Long]]) extends GroupService {

  override def exists(id: Long, groupType: MemberTypes.Value): Boolean =
    groupIds.getOrElse(groupType, Seq()) contains id

}
