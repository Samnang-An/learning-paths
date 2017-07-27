package com.arcusys.valamis.learningpath.services

import com.arcusys.valamis.learningpath.models.{Goal, GoalLRActivity}
import org.joda.time.Period

import scala.concurrent.Future

/**
  * Created by mminin on 20/01/2017.
  */
trait GoalActivityService {

  def create(learningPathId: Long,
             timeLimit: Option[Period],
             optional: Boolean,
             activityName: String,
             count: Int)
            (implicit companyId: Long): Future[(Goal, GoalLRActivity)]

  def createInGroup(parentGroupId: Long,
                    timeLimit: Option[Period],
                    optional: Boolean,
                    activityName: String,
                    count: Int)
                   (implicit companyId: Long): Future[(Goal, GoalLRActivity)]

  def update(goalId: Long,
             timeLimit: Option[Period],
             optional: Boolean,
             count: Int)
            (implicit companyId: Long): Future[(Goal, GoalLRActivity)]
}
