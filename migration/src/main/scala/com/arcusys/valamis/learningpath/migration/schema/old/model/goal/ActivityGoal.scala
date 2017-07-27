package com.arcusys.valamis.learningpath.migration.schema.old.model.goal

private[migration] case class ActivityGoal(goalId: Long,
                        certificateId: Long,
                        activityName: String,
                        count: Int) extends Goal