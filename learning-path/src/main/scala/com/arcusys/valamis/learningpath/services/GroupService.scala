package com.arcusys.valamis.learningpath.services

import com.arcusys.valamis.members.picker.model.MemberTypes

/**
  * Created by pkornilov on 6/1/17.
  */
trait GroupService {

  def exists(id: Long, groupType: MemberTypes.Value): Boolean

}
