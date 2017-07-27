package com.arcusys.valamis.learningpath.listeners

import com.arcusys.valamis.learningpath.Configuration
import com.arcusys.valamis.learningpath.utils.LiferayLogSupport
import com.liferay.portal.kernel.log.{Log, LogFactoryUtil}
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
