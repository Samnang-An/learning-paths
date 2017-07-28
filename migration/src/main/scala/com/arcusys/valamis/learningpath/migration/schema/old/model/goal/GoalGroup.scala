package com.arcusys.valamis.learningpath.migration.schema.old.model.goal

import com.arcusys.valamis.learningpath.migration.schema.old.model.PeriodTypes.PeriodType
import org.joda.time.DateTime

private[migration] case class GoalGroup(id: Long,
                     count: Int,
                     certificateId: Long,
                     periodValue: Int,
                     periodType: PeriodType,
                     arrangementIndex: Int,
                     modifiedDate: DateTime,
                     userId: Option[Long],
                     isDeleted: Boolean = false)