package com.arcusys.valamis.learningpath.services

import com.arcusys.valamis.learningpath.models.{LPVersion, LearningPath}

import scala.concurrent.Future

/**
  * Created by mminin on 06/03/2017.
  */
trait LPVersionService {

  def createNewDraft(learningPathId: Long)
                    (implicit companyId: Long): Future[(LearningPath, LPVersion)]
}
