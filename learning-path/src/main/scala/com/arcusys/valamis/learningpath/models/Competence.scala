package com.arcusys.valamis.learningpath.models

/**
  * Created by pkornilov on 6/6/17.
  */
case class Competence(skillId: Long,
                      skillName: String,
                      levelId: Long,
                      levelName: String)

case class CompetenceDbEntity(versionId: Long,
                              skillId: Long,
                              skillName: String,
                              levelId: Long,
                              levelName: String) {

  def this(versionId: Long, data: Competence) {
    this(
      versionId = versionId,
      skillId = data.skillId,
      skillName = data.skillName,
      levelId = data.levelId,
      levelName = data.levelName
    )
  }
}

object CompetenceMessageActions {
  val SkillChanged = "skillChanged"
  val SkillDeleted = "skillDeleted"

  val LevelChanged = "levelChanged"
  val LevelDeleted = "levelDeleted"
}