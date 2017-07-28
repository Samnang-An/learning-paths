package com.arcusys.valamis.learningpath.impl

import java.io.Serializable

import com.arcusys.valamis.learningpath.impl.utils.LiferayLogSupport
import com.arcusys.valamis.learningpath.init.Configuration
import com.arcusys.valamis.learningpath.listeners.UserLPStatusListener
import com.arcusys.valamis.learningpath.services.impl.{CourseUserStatusServiceImpl, LessonServiceImpl, TrainingEventServiceBridgeImpl, UserLPStatusModelListener}
import com.arcusys.valamis.learningpath.tasks.TaskManagerBaseImpl
import com.arcusys.valamis.learningpath.utils.DbActions
import com.liferay.portal.background.task.service.BackgroundTaskLocalServiceUtil
import com.liferay.portal.kernel.service.{ServiceContext, UserLocalServiceUtil}

import scala.concurrent.ExecutionContext

class TaskManagerImpl(implicit val executionContext: ExecutionContext)
  extends TaskManagerBaseImpl
    with LiferayLogSupport {

  val dbActions: DbActions = Configuration.dbActions
  val lrActivityService: LRActivityTypeServiceImpl = Configuration.lrActivityService
  val lessonService: LessonServiceImpl = Configuration.lessonService
  val messageBusService: MessageBusServiceImpl = Configuration.messageBusService
  val courseUserStatusService: CourseUserStatusServiceImpl = Configuration.courseUserStatusService
  val trainingEventServiceBridge: TrainingEventServiceBridgeImpl = Configuration.trainingEventServiceBridge
  val userLPStatusModelListener: UserLPStatusModelListener = Configuration.userLPStatusModelListener
  val userLPStatusListener: UserLPStatusListener = Configuration.userLPStatusListener

  override protected def planTask(name: String, data: Map[String, Serializable])
                                 (implicit companyId: Long): Unit = {

    val userId = UserLocalServiceUtil.getDefaultUserId(companyId)

    val payload = new java.util.HashMap[String, java.io.Serializable](data.size + 1)
    data.foreach(e => payload.put(e._1, e._2))
    payload.put(TaskManagerKeys.companyId, companyId)

    BackgroundTaskLocalServiceUtil.addBackgroundTask(
      userId,
      0, //groupId //TODO: use courseId
      name,
      Array[String](Configuration.contextName),
      classOf[TaskExecutor],
      payload,
      new ServiceContext()
    )
  }
}
