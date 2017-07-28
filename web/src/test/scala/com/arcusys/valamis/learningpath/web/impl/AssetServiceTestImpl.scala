package com.arcusys.valamis.learningpath.web.impl

import com.arcusys.valamis.learningpath.models.{LPVersion, LearningPath}
import com.arcusys.valamis.learningpath.services.AssetService

import scala.concurrent.Future

class AssetServiceTestImpl extends AssetService {
  override def create(learningPath: LearningPath, version: LPVersion): Future[Unit] = {
    Future.successful {}
  }

  override def update(learningPath: LearningPath, version: LPVersion): Future[Unit] = {
    Future.successful {}
  }

  override def delete(learningPath: LearningPath, version: LPVersion): Future[Unit] = {
    Future.successful {}
  }
}
