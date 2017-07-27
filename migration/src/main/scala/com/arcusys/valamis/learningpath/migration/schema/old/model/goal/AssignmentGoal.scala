package com.arcusys.valamis.learningpath.migration.schema.old.model.goal

private[migration] case class AssignmentGoal(goalId: Long,
                          certificateId: Long,
                          assignmentId: Long) extends Goal