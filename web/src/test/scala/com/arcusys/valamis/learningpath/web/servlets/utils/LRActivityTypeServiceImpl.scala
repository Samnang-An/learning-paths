package com.arcusys.valamis.learningpath.web.servlets.utils

import com.arcusys.valamis.learningpath.models.LRActivityType
import com.arcusys.valamis.learningpath.services.LRActivityTypeService
import org.joda.time.DateTime

import scala.concurrent.Future

/**
  * Test implementation of LRActivityTypeService
  */
case class LRActivity(userId: Long, activityId: String)

class LRActivityTypeServiceImpl(types: Seq[LRActivityType] = Nil,
                                var activities: Seq[LRActivity] = Nil)
  extends LRActivityTypeService {

  override def getAll(implicit companyId: Long): Future[Seq[LRActivityType]] = {
    Future.successful(types)
  }

  override def getLRActivityCountByUser(userId: Long,
                                        activityId: String,
                                        dateFrom: DateTime)
                                       (implicit companyId: Long): Future[Long] = Future.successful {
    activities
      .count(a => a.userId == userId && a.activityId == activityId)
  }

  def addActivity(activity: LRActivity): Unit = {
    activities = activities :+ activity
  }
}
