package com.arcusys.valamis.learningpath.migration.schema.old

import com.arcusys.valamis.learningpath.migration.schema.old.model.PeriodTypes
import com.arcusys.valamis.learningpath.migration.schema.old.model.goal.GoalGroup
import com.arcusys.valamis.learningpath.services.impl.tables.utils.{SlickProfile, TableHelper}
import org.joda.time.DateTime

private[migration] trait CertificateGoalGroupTableComponent
  extends TableHelper
    with CertificateTableComponent {
  self: SlickProfile =>

  import profile.api._

  trait CertificateGoalHistoryColumns {
    self: LongKeyTable[_] =>
    def modifiedDate = column[DateTime]("MODIFIED_DATE")

    def userId = column[Option[Long]]("USER_ID")

    def isDeleted = column[Boolean]("IS_DELETED")
  }

  implicit lazy val periodTypesMapper = enumerationMapper(PeriodTypes)

  class CertificateGoalGroupTable(tag: Tag)
    extends LongKeyTable[GoalGroup](tag, "LEARN_CERT_GOALS_GROUP")
      with CertificateGoalHistoryColumns {

    def count = column[Int]("COUNT")

    def certificateId = column[Long]("CERTIFICATE_ID")

    def periodValue = column[Int]("PERIOD_VALUE")

    def periodType = column[PeriodTypes.PeriodType]("PERIOD_TYPE")

    def arrangementIndex = column[Int]("ARRANGEMENT_INDEX")

    def * = (id,
      count,
      certificateId,
      periodValue,
      periodType,
      arrangementIndex,
      modifiedDate,
      userId,
      isDeleted) <> (GoalGroup.tupled, GoalGroup.unapply)
  }

  val certificateGoalGroups = TableQuery[CertificateGoalGroupTable]
}