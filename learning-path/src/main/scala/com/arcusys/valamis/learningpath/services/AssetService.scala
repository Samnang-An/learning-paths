package com.arcusys.valamis.learningpath.services

import com.arcusys.valamis.learningpath.models.{LPVersion, LearningPath}

import scala.concurrent.Future

/**
  * Created by mminin on 31/03/2017.
  */
trait AssetService {
  def create(learningPath: LearningPath, version: LPVersion): Future[Unit]

  def update(learningPath: LearningPath, version: LPVersion): Future[Unit]

  def delete(learningPath: LearningPath, version: LPVersion): Future[Unit]
}
