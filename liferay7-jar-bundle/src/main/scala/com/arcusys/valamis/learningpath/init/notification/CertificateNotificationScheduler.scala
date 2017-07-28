package com.arcusys.valamis.learningpath.init.notification

import com.arcusys.valamis.learningpath.listeners.liferay.CertificateShedulerListener
import com.liferay.portal.kernel.messaging.{BaseMessageListener, BaseSchedulerEntryMessageListener, DestinationNames, Message}
import com.liferay.portal.kernel.module.framework.ModuleServiceLifecycle
import com.liferay.portal.kernel.scheduler.{SchedulerEngineHelper, TriggerFactory, TriggerFactoryUtil}
import org.osgi.service.component.annotations._

@Component(immediate = true, service = Array(classOf[BaseMessageListener]))
class CertificateNotificationScheduler
  extends BaseSchedulerEntryMessageListener {

  private var _schedulerEngineHelper: SchedulerEngineHelper = _ //to be injected by Service Component Runtime

  private lazy val listener = new CertificateShedulerListener

  override def doReceive(message: Message): Unit = {
    listener.receive(message)
  }

  @Activate
  @Modified
  protected def activate(properties: java.util.Map[String, Object]) {
    schedulerEntryImpl.setTrigger(
      TriggerFactoryUtil.createTrigger(
        getEventListenerClass, getEventListenerClass,
        "0 0 16 * * ?"))

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
