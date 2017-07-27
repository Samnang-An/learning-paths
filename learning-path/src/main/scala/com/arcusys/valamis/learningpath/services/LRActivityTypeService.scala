package com.arcusys.valamis.learningpath.services

import com.arcusys.valamis.learningpath.models.LRActivityType
import org.joda.time.DateTime

import scala.concurrent.Future

/**
  * Created by mminin on 17/02/2017.
  */
trait LRActivityTypeService {
  def getAll(implicit companyId: Long): Future[Seq[LRActivityType]]

  def getLRActivityCountByUser(userId: Long,
                               activityId: String,
                               dateFrom: DateTime)
                              (implicit companyId: Long): Future[Long]
}
