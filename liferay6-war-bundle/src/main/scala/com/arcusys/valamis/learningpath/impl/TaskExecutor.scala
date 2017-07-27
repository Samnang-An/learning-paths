package com.arcusys.valamis.learningpath.impl

import com.arcusys.valamis.learningpath.Configuration
import com.arcusys.valamis.learningpath.utils.LiferayLogSupport
import com.liferay.portal.kernel.backgroundtask.{BackgroundTaskResult, BaseBackgroundTaskExecutor}
import com.liferay.portal.model.BackgroundTask

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration.Duration


class TaskExecutor extends BaseBackgroundTaskExecutor
  with LiferayLogSupport {

  override def handleException(backgroundTask: BackgroundTask, e: Exception): String = {
    log.error("BG Task Failure, backgroundTask: " + backgroundTask.getTaskExecutorClassName, e)

    super.handleException(backgroundTask, e)
  }

  override def execute(backgroundTask: BackgroundTask): BackgroundTaskResult = {
    implicit val ec = Configuration.executionContext

    val payload = backgroundTask.getTaskContextMap.asScala.toMap
    implicit val companyId = payload(TaskManagerKeys.companyId).toString.toLong


    val action = Configuration.taskManager.runTask(
      backgroundTask.getName,
      payload
    )

    Await.result(action, Duration.Inf)

    BackgroundTaskResult.SUCCESS
  }
}
