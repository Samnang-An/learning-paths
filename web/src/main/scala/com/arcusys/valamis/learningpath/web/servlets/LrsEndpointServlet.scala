package com.arcusys.valamis.learningpath.web.servlets

import javax.servlet.http.HttpServletRequest

import com.arcusys.valamis.learningpath.services.UserService
import com.arcusys.valamis.learningpath.web.servlets.base.LearningPathServletBase
import com.arcusys.valamis.learningpath.web.servlets.response.{LrsAccount, LrsAgent, LrsSettingsResponse}
import com.arcusys.valamis.lrs.tincan.AuthorizationScope
import com.arcusys.valamis.lrssupport.lrs.service.LrsRegistration


import scala.concurrent.ExecutionContext

/**
  * Created by mminin on 21/03/2017.
  */
trait LrsEndpointServlet {
  self: LearningPathServletBase =>

  implicit val executionContext: ExecutionContext

  protected val lrsEndpointPrefix: String

  def lrsRegistration: LrsRegistration

  def getPortalURL(request: HttpServletRequest): String

  def getHomePage(companyId: Long): String

  def valamisContextPath: String

  def userService: UserService

  get(s"$lrsEndpointPrefix/?") {
    val host = getPortalURL(request)
    val endpoint = s"$host/delegate/proxy/"
    val userId = currentUserId
    val cId = companyId


    val auth = lrsRegistration.getLrsEndpointInfo(AuthorizationScope.All,
      host = host)(companyId).auth
    LrsSettingsResponse(
      valamisContextPath,
      endpoint,
      auth,
      LrsAgent(
        userService.getUserName(userId),
        LrsAccount(
          userService.getUserUUID(userId),
          getHomePage(cId)
        )
      )
    )
  }
}
