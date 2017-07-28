package com.arcusys.valamis.learningpath.schedulers

import com.arcusys.valamis.learningpath.impl.utils.LiferayLogSupport
import com.arcusys.valamis.learningpath.init.Configuration
import com.arcusys.valamis.learningpath.tasks.{DeadlineChecker, ExpiredGoalsChecker}
import com.liferay.portal.kernel.messaging.{BaseMessageListener, BaseSchedulerEntryMessageListener, DestinationNames, Message}
import com.liferay.portal.kernel.module.framework.ModuleServiceLifecycle
import com.liferay.portal.kernel.scheduler.{SchedulerEngineHelper, TimeUnit, TriggerFactory, TriggerFactoryUtil}
import org.osgi.service.component.annotations._

import scala.concurrent.Await
import scala.concurrent.duration.Duration


@Component(immediate = true, service = Array(classOf[BaseMessageListener]))
class DeadlineSchedulerComponent
  extends BaseSchedulerEntryMessageListener
    with LiferayLogSupport {

  private var _schedulerEngineHelper: SchedulerEngineHelper = _ //to be injected by Service Component Runtime

  implicit private val execContext = Configuration.executionContext

  private lazy val expiredGoalChecker = new ExpiredGoalsChecker(
    Configuration.dbActions, Configuration.taskManager)

  private lazy val deadlineChecker = new DeadlineChecker(Configuration.dbActions, expiredGoalChecker)

  override def doReceive(message: Message): Unit = {
    try {
      Await.result(deadlineChecker.checkDeadlines(), Duration.Inf)
    } catch {
      case ex: Throwable => log.error("Failed to check deadlines", ex)
    }
  }

  @Activate
  @Modified
  protected def activate(properties: java.util.Map[String, Object]) {
    schedulerEntryImpl.setTrigger(
      TriggerFactoryUtil.createTrigger(
        getEventListenerClass, getEventListenerClass, 30, TimeUnit.MINUTE))

    _schedulerEngineHelper.register(
      this, schedulerEntryImpl, DestinationNames.SCHEDULER_DISPATCH)
  }

  @Deactivate
  protected def deactivate() {
    _schedulerEngineHelper.unregister(this)
  }

  @Reference(unbind = "-")
  protected def setSchedulerEngineHelper(schedulerEngineHelper: SchedulerEngineHelper) {
    _schedulerEngineHelper = schedulerEngineHelper
  }

  //These methods are needed to make sure,
  //that needed Liferay services have been initialized before activating our component
  @Reference(target = ModuleServiceLifecycle.PORTAL_INITIALIZED, unbind = "-")
  protected def setModuleServiceLifecycle(moduleServiceLifecycle: ModuleServiceLifecycle) {}

  @Reference(unbind = "-")
  protected def setTriggerFactory(triggerFactory: TriggerFactory) {}
}
