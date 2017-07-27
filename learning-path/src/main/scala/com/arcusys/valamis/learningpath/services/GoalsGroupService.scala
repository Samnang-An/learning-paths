package com.arcusys.valamis.learningpath.services

import com.arcusys.valamis.learningpath.models.{Goal, GoalGroup}
import org.joda.time.Period

import scala.concurrent.Future

/**
  * Created by mminin on 20/01/2017.
  */
trait GoalsGroupService {

  def get(id: Long)(implicit companyId: Long): Future[Option[(Goal, GoalGroup)]]

  def create(learningPathId: Long,
             title: String,
             timeLimit: Option[Period],
             optional: Boolean,
             count: Option[Int])
            (implicit companyId: Long): Future[(Goal, GoalGroup)]

  def createInGroup(parentGroupId: Long,
                    title: String,
                    timeLimit: Option[Period],
                    optional: Boolean,
                    count: Option[Int])
                   (implicit companyId: Long): Future[(Goal, GoalGroup)]

  def update(groupId: Long,
             title: String,
             timeLimit: Option[Period],
             optional: Boolean,
             count: Option[Int])
            (implicit companyId: Long): Future[(Goal, GoalGroup)]

  def delete(groupId: Long)
            (implicit companyId: Long): Future[Unit]
}
