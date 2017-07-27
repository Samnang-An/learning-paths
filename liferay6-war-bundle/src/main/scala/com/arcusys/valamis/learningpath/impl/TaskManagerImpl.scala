package com.arcusys.valamis.learningpath.impl

import com.arcusys.valamis.learningpath.Configuration
import com.arcusys.valamis.learningpath.listeners.UserLPStatusListener
import com.arcusys.valamis.learningpath.services.impl.{CourseUserStatusServiceImpl, LessonServiceImpl, TrainingEventServiceBridgeImpl, UserLPStatusModelListener}
import com.arcusys.valamis.learningpath.tasks.TaskManagerBaseImpl
import com.arcusys.valamis.learningpath.utils.{DbActions, LiferayLogSupport}
import com.liferay.portal.service.{BackgroundTaskLocalServiceUtil, ServiceContext, UserLocalServiceUtil}

import scala.concurrent.ExecutionContext

/**
  * Created by mminin on 10/03/2017.
  */
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


  override protected def planTask(name: String, data: Map[String, java.io.Serializable])
                                 (implicit companyId: Long): Unit = {

    val userId = UserLocalServiceUtil.getDefaultUserId(companyId)

    val payload = new java.util.HashMap[String, java.io.Serializable](data.size + 1)
    data.foreach(e => payload.put(e._1, e._2))
    payload.put(TaskManagerKeys.companyId, companyId)

    BackgroundTaskLocalServiceUtil.addBackgroundTask(
      userId,
      0, //groupId //TODO: use courseId
      name,
      Array[String]("learning-paths-portlet"),
      classOf[TaskExecutor],
      payload,
      new ServiceContext()
    )
  }

}
