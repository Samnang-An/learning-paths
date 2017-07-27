package com.arcusys.valamis.learningpath.migration.schema.old.model.goal

import com.arcusys.valamis.learningpath.migration.schema.old.model.PeriodTypes.PeriodType
import org.joda.time.DateTime

private[migration] case class CertificateGoal(id: Long,
                           certificateId: Long,
                           goalType: GoalType.Value,
                           periodValue: Int,
                           periodType: PeriodType,
                           arrangementIndex: Int,
                           isOptional: Boolean = false,
                           groupId: Option[Long],
                           oldGroupId: Option[Long],
                           modifiedDate: DateTime,
                           userId: Option[Long],
                           isDeleted: Boolean = false)


private[migration] trait Goal {
  def goalId: Long
  def certificateId: Long
}

private[migration] object GoalType extends Enumeration {
  type GoalType = Value
  val Activity, Course, Statement, Package, Assignment, TrainingEvent = Value
}