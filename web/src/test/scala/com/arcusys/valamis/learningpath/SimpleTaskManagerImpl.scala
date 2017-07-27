package com.arcusys.valamis.learningpath

import java.io.Serializable

import com.arcusys.valamis.learningpath.listeners.UserLPStatusListener
import com.arcusys.valamis.learningpath.services._
import com.arcusys.valamis.learningpath.services.impl.UserLPStatusModelListener
import com.arcusys.valamis.learningpath.tasks._
import com.arcusys.valamis.learningpath.utils.DbActions
import org.apache.commons.logging.Log
import org.joda.time.DateTime
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

/**
  * Created by mminin on 10/03/2017.
  */
class SimpleTaskManagerImpl(db: JdbcBackend#DatabaseDef,
                            profile: JdbcProfile,
                            val log: Log,
                            val dbActions: DbActions,
                            val lrActivityService: LRActivityTypeService,
                            val lessonService: LessonService,
                            val courseUserStatusService: CourseUserStatusService,
                            val trainingEventServiceBridge: TrainingEventServiceBridge,
                            val messageBusService: MessageBusService,
                            val userLPStatusModelListener: UserLPStatusModelListener,
                            now: => DateTime)
                           (implicit val executionContext: ExecutionContext) extends TaskManagerBaseImpl {

  override protected def planTask(name: String, data: Map[String, Serializable])
                                 (implicit companyId: Long): Unit = {
    // in test env. we run task immediately and wait for completion
    Await.result(
      runTask(name, data.updated(TaskManagerKeys.now, now)),
      Duration.Inf
    )
  }

  override protected def userLPStatusListener: UserLPStatusListener = new TestUserLPStatusListener
}
