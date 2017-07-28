package com.arcusys.valamis.learningpath.services

import com.arcusys.valamis.learningpath.models.patternreport.SkipTake
import com.arcusys.valamis.lrs.tincan.Agent

/**
  * Created by mminin on 22/03/2017.
  */
trait UserService {
  def getUserName(userId: Long): String

  def getUserUUID(userId: Long): String

  def search(keyword: Option[String],
             skipTake: Option[SkipTake])
            (implicit companyId: Long): Seq[Long]

  def getAgent(userId: Long,
               companyId: Long,
               homePage: String): Agent
}
