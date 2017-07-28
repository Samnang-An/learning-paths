package com.arcusys.valamis.learningpath.services

import java.util.UUID

import com.arcusys.valamis.learningpath.models.LPVersion

trait LPStatementService {
  def sendStatementCompleted(userId: Long,
                             companyId: Long,
                             lpVersion: LPVersion): Option[UUID]

  def sendStatementAddedUser(userId: Seq[Long],
                             companyId: Long,
                             lpVersion: LPVersion): Unit

  def createActivityId(id: Long, companyId: Long): String

}