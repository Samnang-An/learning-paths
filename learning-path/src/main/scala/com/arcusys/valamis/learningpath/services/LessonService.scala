package com.arcusys.valamis.learningpath.services

import com.arcusys.valamis.learningpath.models.LessonStatus

import scala.concurrent.Future

/**
  * Created by mminin on 21/02/2017.
  */
trait LessonService {

  def getLessonNames(ids: Seq[Long]): Future[Map[Long, String]]

  def isCompleted(id: Long, userId: Long)(implicit companyId: Long): Future[Boolean]

  def isValamisDeployed: Boolean
}
