package com.arcusys.valamis.learningpath.services.impl.tables.competences

import com.arcusys.valamis.learningpath.models.{Competence, CompetenceDbEntity}
import com.arcusys.valamis.learningpath.services.impl.tables.LPVersionTableComponent
import com.arcusys.valamis.learningpath.services.impl.tables.utils.{SlickProfile, TableHelper}

/**
  * Created by pkornilov on 6/6/17.
  */
trait CompetencesTableComponent extends TableHelper {
  self: SlickProfile with LPVersionTableComponent =>

  import profile.api._

  abstract class CompetenceTable(tag: Tag, name: String, shortName: String)
    extends Table[CompetenceDbEntity](tag, tblName(name))
    with LPVersionLinkSupport {

    val skillId = column[Long]("SKILL_ID")
    val skillName = column[String]("SKILL_NAME", O.Length(titleSize, varying = true))

    val levelId = column[Long]("LEVEL_ID")
    val levelName = column[String]("LEVEL_NAME", O.Length(titleSize, varying = true))

    def * = (versionId, skillId, skillName, levelId, levelName) <>
      (CompetenceDbEntity.tupled, CompetenceDbEntity.unapply)

    def withoutVersionId = (skillId, skillName, levelId, levelName) <>
      (Competence.tupled, Competence.unapply)

    def pk = primaryKey(s"PK_$name", (versionId, skillId))

    def versionIdx = index(idxName(shortName, "version"), versionId)

    override val version = foreignKey(fkName(shortName, versionTQ.baseTableRow.tableName),
      versionId, versionTQ)(_.id)
  }

  class RecommendedCompetenceTable(tag: Tag) extends CompetenceTable(tag, "RECOMMENDED_COMPETENCES", "RCMNDED_COMPTNCS")
  class ImprovingCompetenceTable(tag: Tag) extends CompetenceTable(tag, "IMPROVING_COMPETENCES", "IMPRVNG_COMPTNCS")

  lazy val recommendedCompetencesTQ = TableQuery[RecommendedCompetenceTable]
  lazy val improvingCompetencesTQ = TableQuery[ImprovingCompetenceTable]
}
