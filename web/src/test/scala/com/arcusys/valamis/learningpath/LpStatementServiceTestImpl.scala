package com.arcusys.valamis.learningpath

import java.util.UUID

import com.arcusys.valamis.learningpath.models.LPVersion
import com.arcusys.valamis.learningpath.services.LPStatementService

class LpStatementServiceTestImpl extends LPStatementService  {

  override def sendStatementCompleted(userId: Long,
                                      companyId: Long,
                                      lpVersion: LPVersion): Option[UUID] = None

  override def sendStatementAddedUser(userId: Seq[Long],
                                      companyId: Long,
                                      lpVersion: LPVersion): Unit = {}

  override def createActivityId(id: Long, companyId: Long): String = ""
}
