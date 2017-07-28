package com.arcusys.valamis.learningpath.migration.schema.old.model.goal

private[migration] case class CourseGoal(goalId: Long,
                      certificateId: Long,
                      courseId: Long) extends Goal