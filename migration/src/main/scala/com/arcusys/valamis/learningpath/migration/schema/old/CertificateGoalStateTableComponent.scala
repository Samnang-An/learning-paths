package com.arcusys.valamis.learningpath.migration.schema.old

import com.arcusys.valamis.learningpath.migration.schema.old.model.goal.{CertificateGoalState, GoalStatuses}
import com.arcusys.valamis.learningpath.services.impl.tables.utils.{SlickProfile, TableHelper}
import org.joda.time.DateTime

private[migration] trait CertificateGoalStateTableComponent
  extends LongKeyTableComponent
    with TableHelper
    with CertificateTableComponent
    with CertificateGoalTableComponent{ self: SlickProfile =>

  import profile.api._

  implicit lazy val goalStatusTypeMapper = enumerationMapper(GoalStatuses)

  class CertificateGoalStateTable(tag: Tag) extends Table[CertificateGoalState](tag, "LEARN_CERT_GOALS_STATE") {
    def userId = column[Long]("USER_ID")
    def certificateId = column[Long]("CERTIFICATE_ID")
    def goalId = column[Long]("GOAL_ID")
    def status = column[GoalStatuses.Value]("STATUS")
    def modifiedDate = column[DateTime]("MODIFIED_DATE")
    def isOptional = column[Boolean]("IS_OPTIONAL")

    def * = (userId, certificateId, goalId, status, modifiedDate, isOptional) <> (CertificateGoalState.tupled, CertificateGoalState.unapply)

  }

  val certificateGoalStates = TableQuery[CertificateGoalStateTable]
}
