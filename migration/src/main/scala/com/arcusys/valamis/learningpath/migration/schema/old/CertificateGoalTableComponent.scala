package com.arcusys.valamis.learningpath.migration.schema.old

import com.arcusys.valamis.learningpath.migration.schema.old.model.PeriodTypes
import com.arcusys.valamis.learningpath.migration.schema.old.model.goal.{CertificateGoal, GoalType}
import com.arcusys.valamis.learningpath.services.impl.tables.utils.{SlickProfile, TableHelper}

private[migration] trait CertificateGoalTableComponent
  extends LongKeyTableComponent
    with TableHelper
    with CertificateTableComponent
    with CertificateGoalGroupTableComponent { self: SlickProfile =>

  import profile.api._

  implicit lazy val certificateGoalTypeMapper = enumerationIdMapper(GoalType)
  implicit lazy val validPeriodTypeMapper = enumerationMapper(PeriodTypes)

  trait CertificateGoalBaseColumns { self: Table[_] =>
    def goalId = column[Long]("GOAL_ID")
    def certificateId = column[Long]("CERTIFICATE_ID")

  }

  class CertificateGoalTable(tag: Tag)
    extends LongKeyTable[CertificateGoal](tag, "LEARN_CERT_GOALS")
      with CertificateGoalHistoryColumns {

    def certificateId = column[Long]("CERTIFICATE_ID")
    def goalType = column[GoalType.Value]("GOAL_TYPE")
    def periodValue = column[Int]("PERIOD_VALUE")
    def periodType = column[PeriodTypes.PeriodType]("PERIOD_TYPE")
    def arrangementIndex = column[Int]("ARRANGEMENT_INDEX")
    def isOptional = column[Boolean]("IS_OPTIONAL")
    def groupId = column[Option[Long]]("GROUP_ID")
    def oldGroupId = column[Option[Long]]("OLD_GROUP_ID")

    def * = (id,
      certificateId,
      goalType,
      periodValue,
      periodType,
      arrangementIndex,
      isOptional,
      groupId,
      oldGroupId,
      modifiedDate,
      userId,
      isDeleted) <> (CertificateGoal.tupled, CertificateGoal.unapply)

  }

  val certificateGoals = TableQuery[CertificateGoalTable]
}