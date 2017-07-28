package com.arcusys.valamis.learningpath.migration.schema.old.model.goal

private[migration] case class PackageGoal(goalId: Long,
                       certificateId: Long,
                       packageId: Long) extends Goal