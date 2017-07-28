package com.arcusys.valamis.learningpath.services.impl.tables

import com.arcusys.valamis.learningpath.models.{Goal, GoalTypes}
import com.arcusys.valamis.learningpath.services.impl.tables.utils.{SlickProfile, TableHelper}
import org.joda.time.{DateTime, Period}

/**
  * Created by mminin on 23/01/2017.
  */
trait GoalTableComponent extends TableHelper {
  self: SlickProfile
    with LPVersionTableComponent =>

  import profile.api._

  implicit lazy val goalTypesMapper = enumerationIdMapper(GoalTypes)

  lazy val goalTQ = TableQuery[GoalTable]

  trait GoalLinkSupport {
    self: Table[_] =>
    val goalId = column[Long]("GOAL_ID")

    def goal = foreignKey(
      name = fkName(tableName, goalTQ.baseTableRow.tableName),
      sourceColumns = goalId,
      targetTableQuery = goalTQ
    )(
      targetColumns = _.id,
      onDelete = ForeignKeyAction.Cascade
    )
  }

  class GoalTable(tag: Tag) extends Table[Goal](tag, tblName("GOAL"))
    with IdentitySupport
    with LPVersionLinkSupport {

    val oldGoalId = column[Option[Long]]("OLD_GOAL_ID")
    val groupId = column[Option[Long]]("GROUP_ID")
    val goalType = column[GoalTypes.Value]("GOAL_TYPE")
    val indexNumber = column[Int]("INDEX_NUMBER")
    val timeLimit = column[Option[Period]]("TIME_LIMIT")
    val optional = column[Boolean]("OPTIONAL")
    val modifiedDate = column[DateTime]("MODIFIED_DATE")

    def group = foreignKey(fkName(tableName, goalTQ.baseTableRow.tableName),
      groupId, goalTQ)(_.id.?)

    def groupIdx = index(idxName(tableName, "group"), groupId)
    def versionIdx = index(idxName(tableName, "version"), versionId)

    def * = (id, oldGoalId, versionId, groupId, goalType,
      indexNumber, timeLimit, optional, modifiedDate) <>
      (Goal.tupled, Goal.unapply)
  }

}
