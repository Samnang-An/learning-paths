package com.arcusys.valamis.learningpath.migration.schema.old.model.goal

private[migration] case class StatementGoal(goalId: Long,
                         certificateId: Long,
                         verb: String,
                         obj: String) extends Goal