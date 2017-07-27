package com.arcusys.valamis.learningpath.schedulers

import com.arcusys.valamis.learningpath.Configuration
import com.arcusys.valamis.learningpath.tasks.{DeadlineChecker, ExpiredGoalsChecker}
import com.arcusys.valamis.learningpath.utils.LiferayLogSupport
import com.liferay.portal.kernel.messaging.{Message, MessageListener}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class DeadlineScheduler
  extends MessageListener
    with LiferayLogSupport {

  implicit private val execContext = Configuration.executionContext

  private lazy val expiredGoalChecker = new ExpiredGoalsChecker(
    Configuration.dbActions, Configuration.taskManager)

  private lazy val deadlineChecker = new DeadlineChecker(Configuration.dbActions, expiredGoalChecker)


  override def receive(message: Message): Unit = {
    try {
      Await.result(deadlineChecker.checkDeadlines(), Duration.Inf)
    } catch {
      case ex: Throwable => log.error("Failed to check deadlines", ex)
    }
  }

}
