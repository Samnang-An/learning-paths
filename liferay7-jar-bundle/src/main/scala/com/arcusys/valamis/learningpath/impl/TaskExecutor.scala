package com.arcusys.valamis.learningpath.impl

import com.arcusys.valamis.learningpath.impl.utils.LiferayLogSupport
import com.arcusys.valamis.learningpath.init.Configuration
import com.liferay.portal.kernel.backgroundtask.display.{BackgroundTaskDisplay, BaseBackgroundTaskDisplay}
import com.liferay.portal.kernel.backgroundtask.{BackgroundTask, BackgroundTaskExecutor, BackgroundTaskResult, BaseBackgroundTaskExecutor}
import com.liferay.portal.kernel.template.TemplateResource

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration.Duration


class TaskExecutor extends BaseBackgroundTaskExecutor
  with LiferayLogSupport {

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

  override def getBackgroundTaskDisplay(backgroundTask: BackgroundTask): BackgroundTaskDisplay = {
    new BaseBackgroundTaskDisplay(backgroundTask){
      override def getTemplateVars: java.util.Map[String, AnyRef] = new java.util.HashMap[String, AnyRef]

      override def getPercentage: Int = 0

      override def getTemplateResource: TemplateResource = null
    }
  }

  override def clone(): BackgroundTaskExecutor = super.clone().asInstanceOf[BackgroundTaskExecutor]
}
