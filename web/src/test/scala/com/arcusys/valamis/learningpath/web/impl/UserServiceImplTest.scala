package com.arcusys.valamis.learningpath.web.impl

import com.arcusys.valamis.learningpath.models.patternreport.SkipTake
import com.arcusys.valamis.learningpath.services.UserService
import com.arcusys.valamis.lrs.tincan.{Account, Agent}
import com.arcusys.valamis.members.picker.service.LiferayHelper

/**
  * Created by amikhailov on 04/04/2017.
  */
class UserServiceImplTest(liferayHelper: LiferayHelper) extends UserService {

  override def getUserName(userId: Long): String = {
    liferayHelper.getUserInfo(userId, null).logo
  }

  override def getUserUUID(userId: Long): String = ???

  override def search(keyword: Option[String], skipTake: Option[SkipTake])(implicit companyId: Long): Seq[Long] = {
    liferayHelper
      .getUsers(companyId)
      .filter { user =>
        keyword match {
          case None => true
          case Some(word) => user.name.contains(word)
        }
      }.map(x => x.id)
  }

  override def getAgent(userId: Long, companyId: Long, homePage: String): Agent = {
    val user = liferayHelper.getUserInfo(userId, Seq())
    val account = Account(homePage, getUserUUID(userId))
    Agent(name = Option(user.name), account = Option(account))
  }
}
