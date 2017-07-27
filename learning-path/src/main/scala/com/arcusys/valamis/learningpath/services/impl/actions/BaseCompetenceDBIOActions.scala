package com.arcusys.valamis.learningpath.services.impl.actions

import com.arcusys.valamis.learningpath.models.{Competence, CompetenceDbEntity}
import com.arcusys.valamis.learningpath.services.impl.tables.LearningPathTables
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickProfile
import slick.driver.JdbcProfile


/**
  * Created by pkornilov on 6/6/17.
  */
abstract class BaseCompetenceDBIOActions(val profile: JdbcProfile)
  extends LearningPathTables
    with SlickProfile {

  import profile.api._

  def competencesTQ: TableQuery[_ <: CompetenceTable]

  def insert(competence: CompetenceDbEntity): DBIO[Int] = {
    competencesTQ += competence
  }

  def delete(versionId: Long, skillId: Long): DBIO[Int] = {
    selectByVersionIdAndSkillIdQ(versionId, skillId).delete
  }

  def deleteByLearningPathId(lpId: Long): DBIO[Int] = {
    selectByLearningPathIdQ(lpId).delete
  }

  def getByVersionId(versionId: Long): DBIO[Seq[Competence]] = {
    selectByVersionIdQ(versionId).result
  }

  def updateSkillName(skillId: Long, skillName: String): DBIO[Int] = {
    selectSkillNameBySkillId(skillId).update(skillName)
  }

  def updateLevelName(levelId: Long, levelName: String): DBIO[Int] = {
    selectLevelNameByLevelId(levelId).update(levelName)
  }

  private val selectSkillNameBySkillId = Compiled { skillId: Rep[Long] =>
    competencesTQ filter (_.skillId === skillId) map (_.skillName)
  }

  private val selectLevelNameByLevelId = Compiled { levelId: Rep[Long] =>
    competencesTQ filter (_.levelId === levelId) map (_.levelName)
  }

  private val selectByVersionIdQ = Compiled { versionId: Rep[Long] =>
    competencesTQ filter (_.versionId === versionId) sortBy (_.skillName) map (_.withoutVersionId)
  }

  private val selectByLearningPathIdQ = Compiled { learningPathId: Rep[Long] =>
    competencesTQ
      .filter(_.versionId in versionTQ.filter(_.learningPathId === learningPathId).map(_.id))
  }

  private val selectByVersionIdAndSkillIdQ = Compiled {
    (versionId: Rep[Long], skillId: Rep[Long]) =>
    competencesTQ filter { c =>
      c.versionId === versionId && c.skillId === skillId
    } map (_.withoutVersionId)
  }

}
