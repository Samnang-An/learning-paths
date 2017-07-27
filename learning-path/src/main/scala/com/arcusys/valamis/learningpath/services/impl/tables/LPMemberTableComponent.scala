package com.arcusys.valamis.learningpath.services.impl.tables

import com.arcusys.valamis.learningpath.services.impl.tables.utils.{SlickProfile, TableHelper}
import com.arcusys.valamis.members.picker.schema.MemberTableComponent

/**
  * Created by mminin on 22/02/2017.
  */
trait LPMemberTableComponent extends TableHelper with MemberTableComponent {
  self: SlickProfile
    with LeaningPathTableComponent =>

  import profile.api._

  lazy val membersTQ = TableQuery[LPMemberTable]
  lazy val usersMembershipTQ = TableQuery[LPUserMembershipTable]

  class LPMemberTable(tag: Tag) extends MemberTable(tag, tblName("MEMBER")) {
    val learningPath = foreignKey(
      name = fkName(tableName, learningPathTQ.baseTableRow.tableName),
      sourceColumns = entityId,
      targetTableQuery = learningPathTQ
    )(_.id)

    def lpIdx = index(idxName(tableName, "lp"), entityId)
    def lpAndTypeIndx = index(idxName(tableName, "lp_type"), (entityId, memberType))
  }

  class LPUserMembershipTable(tag: Tag) extends UserMembershipTable(tag, tblName("USR_MEMBER")) {
    val learningPath = foreignKey(
      name = fkName(tableName, learningPathTQ.baseTableRow.tableName),
      sourceColumns = entityId,
      targetTableQuery = learningPathTQ
    )(_.id)

    def lpIdx = index(idxName(tableName, "lp"), entityId)
    def lpAndUserIdx = index(idxName(tableName, "lp_user"), (entityId, userId))
  }
}
