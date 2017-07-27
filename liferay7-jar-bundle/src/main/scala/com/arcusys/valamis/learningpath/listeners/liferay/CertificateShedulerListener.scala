package com.arcusys.valamis.learningpath.listeners.liferay

import com.arcusys.valamis.learningpath.impl.utils.LiferayLogSupport
import com.arcusys.valamis.learningpath.init.Configuration
import com.liferay.portal.kernel.messaging.{Message, MessageListener}

class CertificateShedulerListener
  extends MessageListener
    with LiferayLogSupport {

  override def receive(message: Message): Unit = {
    try {
      Configuration.certificateShedulerService.doAction()
    } catch {
      case e: Throwable =>
        log.error("Failed to send notifications", e)
    }
  }
}
