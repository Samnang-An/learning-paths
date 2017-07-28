package com.arcusys.valamis.learningpath.migration.schema.old.model.goal

import org.joda.time.DateTime

private[migration] case class GoalDeadline[T](goal: T, deadline: Option[DateTime])
