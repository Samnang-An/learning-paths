package com.arcusys.valamis.learningpath.services.impl

import java.util.UUID

import com.arcusys.valamis.learningpath.models.LPVersion
import com.arcusys.valamis.learningpath.services.{CompanyUtil, LPStatementService, UserService}
import com.arcusys.valamis.lrs.tincan._
import com.arcusys.valamis.lrssupport.lrs.service.{LrsClientManager, LrsRegistration}
import org.joda.time.DateTime


abstract class LPStatementServiceImpl
  extends LPStatementService {

  protected def lrsClientManager: LrsClientManager with CompanyUtil

  protected def lrsRegistration: LrsRegistration

  protected def userService: UserService

  def sendStatementCompleted(userId: Long,
                             companyId: Long,
                             lpVersion: LPVersion): Option[UUID] = {
    val verb = Verb("http://adlnet.gov/expapi/verbs/completed", Map("en-US" -> "completed"))
    val statement = Statement(
      Option(UUID.randomUUID),
      userService.getAgent(userId,
        companyId,
        lrsClientManager.getHomePage(companyId)),
      verb,
      Activity(
        id = createActivityId(lpVersion.learningPathId, companyId),
        name = Some(Map("en-US" -> lpVersion.title)),
        theType = Some("http://adlnet.gov/expapi/activities/objective"),
        description = Some(Map("en-US" -> lpVersion.description.getOrElse("")))),
      timestamp = DateTime.now,
      stored = DateTime.now)


    val lrsAuth = lrsRegistration.getLrsEndpointInfo(AuthorizationScope.All,
      host = lrsClientManager.getHostWithPort(companyId))(companyId).auth

    lrsClientManager.statementApi(_.addStatement(statement), Some(lrsAuth))(companyId)
    statement.id
  }


  def sendStatementAddedUser(userIds: Seq[Long],
                             companyId: Long,
                             lpVersion: LPVersion): Unit = {


    val lrsAuth = lrsRegistration.getLrsEndpointInfo(AuthorizationScope.All,
      host = lrsClientManager.getHostWithPort(companyId))(companyId).auth

    userIds.foreach { userId =>


      val verb = Verb("http://adlnet.gov/expapi/verbs/registered", Map("en-US" -> "registered"))
      val statement = Statement(
        Option(UUID.randomUUID),
        userService.getAgent(userId,
          companyId,
          lrsClientManager.getHomePage(companyId)),
        verb,
        Activity(
          id = createActivityId(lpVersion.learningPathId, companyId),
          name = Some(Map("en-US" -> lpVersion.title)),
          theType = Some("http://adlnet.gov/expapi/activities/objective"),
          description = Some(Map("en-US" -> lpVersion.description.getOrElse("")))),
        timestamp = DateTime.now,
        stored = DateTime.now)

      lrsClientManager.statementApi(_.addStatement(statement), Some(lrsAuth))(companyId)
    }
  }

  def createActivityId(id: Long, companyId: Long): String = {
    // TODO maybe add it to TincanURIService.
    val uriValamis = "valamis"
    val uriType = "learning-paths"
    val prefix = lrsClientManager.getHostWithPort(companyId)
    s"$prefix/$uriValamis/$uriType/$id"
  }
}