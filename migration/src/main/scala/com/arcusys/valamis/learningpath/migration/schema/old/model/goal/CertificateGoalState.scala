package com.arcusys.valamis.learningpath.migration.schema.old.model.goal

import org.joda.time.DateTime

private[migration] case class CertificateGoalState(userId: Long,
                                certificateId: Long,
                                goalId: Long,
                                status: GoalStatuses.Value,
                                modifiedDate: DateTime,
                                isOptional: Boolean = false)



