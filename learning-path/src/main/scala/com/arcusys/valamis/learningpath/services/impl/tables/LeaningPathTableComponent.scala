package com.arcusys.valamis.learningpath.services.impl.tables

import com.arcusys.valamis.learningpath.models.LearningPath
import com.arcusys.valamis.learningpath.services.impl.tables.utils.{SlickProfile, TableHelper}

/**
  * Created by mminin on 23/01/2017.
  */
trait LeaningPathTableComponent extends TableHelper {
  self: SlickProfile =>

  import profile.api._

  lazy val learningPathTQ = TableQuery[LearningPathTable]

  trait LearningPathLinkSupport {
    self: Table[_] =>
    val learningPathId = column[Long]("LEARNING_PATH_ID")

    val learningPath = foreignKey(fkName(tableName, learningPathTQ.baseTableRow.tableName),
      learningPathId, learningPathTQ)(_.id)
  }

  class LearningPathTable(tag: Tag) extends Table[LearningPath](tag, tblName("LEARNING_PATH"))
    with IdentitySupport {

    val hasDraft = column[Boolean]("HAS_DRAFT")
    val currentVersionId = column[Option[Long]]("CURRENT_VERSION_ID")
    val activated = column[Boolean]("ACTIVATED")
    val companyId = column[Long]("COMPANY_ID")
    val userId = column[Long]("USER_ID")

    def * = (id, activated, companyId, userId, hasDraft, currentVersionId) <>
      (LearningPath.tupled, LearningPath.unapply)
  }

  /**
    * Used for migration old data
    * @param tag
    */
  class LearningPathTableWithoutAutoInc(tag: Tag) extends LearningPathTable(tag) {
    override val id = column[Long](idName, O.PrimaryKey)
  }

}
