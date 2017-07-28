package com.arcusys.valamis.learningpath.services

import com.arcusys.valamis.learningpath.models.Competence

import scala.concurrent.Future

/**
  * Created by pkornilov on 6/6/17.
  */
trait CompetenceService {

  def getCompetencesByVersionId(versionId: Long)
                               (implicit companyId: Long): Future[Seq[Competence]]

  def getCompetencesForLPLastDraft(learningPathId: Long)
                                  (implicit companyId: Long): Future[Seq[Competence]]

  def getCompetencesForLPCurrentVersion(learningPathId: Long)
                                       (implicit companyId: Long): Future[Seq[Competence]]

  def create(learningPathId: Long,
             competence: Competence)
            (implicit companyId: Long): Future[Unit]

  def delete(learningPathId: Long,
             skillId: Long)
            (implicit companyId: Long): Future[Unit]

  def updateSkillName(skillId: Long,
                      skillName: String)
                     (implicit companyId: Long): Future[Unit]

  def updateLevelName(levelId: Long,
                      levelName: String)
                     (implicit companyId: Long): Future[Unit]

}
