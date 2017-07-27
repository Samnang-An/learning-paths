package com.arcusys.valamis.learningpath.migration.schema.old.model.goal

import org.joda.time.DateTime

private[migration] case class GoalStatus[T](goal: T, status: GoalStatuses.Value, finishDate: Option[DateTime])