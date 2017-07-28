package com.arcusys.valamis.learningpath.migration.schema.old.model.goal

private[migration] case class TrainingEventGoal(goalId: Long,
                             certificateId: Long,
                             eventId: Long) extends Goal