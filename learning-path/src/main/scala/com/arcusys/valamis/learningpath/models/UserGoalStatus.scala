package com.arcusys.valamis.learningpath.models

import org.joda.time.DateTime

/**
  * Goal progress by user, should be create for all goals in published LP
  * include goal groups
  * @param requiredCount how many goals need to complete,
  *                      in most cases is 1,
  *                      not 1 can be in groups goal
  *                      (also not 1 can be fo LRActivity goal) ?
  * @param completedCount now many goals completed
  */
case class UserGoalStatus(userId: Long,
                          goalId: Long,
                          status: GoalStatuses.Value,
                          startedDate: DateTime,
                          modifiedDate: DateTime,
                          requiredCount: Int,
                          completedCount: Int,
                          endDate: Option[DateTime])

case class UserLPStatus(userId: Long,
                        learningPathId: Long,
                        versionId: Long,
                        status: CertificateStatuses.Value,
                        startedDate: DateTime,
                        modifiedDate: DateTime,
                        progress: Double)