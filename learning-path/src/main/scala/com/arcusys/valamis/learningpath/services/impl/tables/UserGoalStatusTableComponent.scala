package com.arcusys.valamis.learningpath.services.impl.tables

import com.arcusys.valamis.learningpath.models.{GoalStatuses, UserGoalStatus}
import com.arcusys.valamis.learningpath.services.impl.tables.utils.{SlickProfile, TableHelper}
import org.joda.time.DateTime

/**
  * Created by mminin on 23/01/2017.
  */
trait UserGoalStatusTableComponent extends TableHelper {
  self: SlickProfile
    with GoalTableComponent =>

  import profile.api._

  implicit lazy val goalStatusesMapper = enumerationIdMapper(GoalStatuses)

  lazy val userGoalStatusTQ = TableQuery[UserStatusTable]

  class UserStatusTable(tag: Tag) extends Table[UserGoalStatus](tag, tblName("USER_GOAL_STATUS"))
    with GoalLinkSupport {

    val userId = column[Long]("USER_ID")
    val status = column[GoalStatuses.Value]("STATUS")
    val startedDate = column[DateTime]("STARTED_DATE")
    val modifiedDate = column[DateTime]("MODIFIED_DATE")
    val requiredCount = column[Int]("REQUIRED_COUNT")
    val completedCount = column[Int]("COMPLETED_COUNT")
    val endDate = column[Option[DateTime]]("END_DATE")

    def pk = primaryKey("PK_LP_USER_GOAL_STATUS", (goalId, userId))

    def userAndStatusIdx = index(idxName(tableName, "u_status"), (userId, status))
    def userAndGoalIdx = index(idxName(tableName, "u_goal"), (userId, goalId))

    override def * = (
      userId, goalId, status, startedDate, modifiedDate, requiredCount, completedCount, endDate
    ) <> (UserGoalStatus.tupled, UserGoalStatus.unapply)
  }

}
