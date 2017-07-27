package com.arcusys.valamis.learningpath.init

import com.arcusys.valamis.learningpath.impl.utils.LiferayLogSupport
import com.arcusys.valamis.learningpath.init.Configuration.dbActions
import com.arcusys.valamis.learningpath.listeners._
import com.arcusys.valamis.learningpath.listeners.liferay._
import com.arcusys.valamis.learningpath.services.MessageBusDestinations
import com.liferay.portal.kernel.messaging.{Destination, MessageListener, SerialDestination}
import com.liferay.portal.kernel.util.ClassLoaderPool
import org.osgi.framework.{BundleActivator, BundleContext}

import scala.collection.JavaConverters._
import scala.language.postfixOps

class Activator extends BundleActivator with LiferayLogSupport {

  import Configuration.executionContext

  private val messageListeners = Map(
    MessageBusDestinations.AssignmentCompleted -> new AssignmentMessageListener,
    MessageBusDestinations.LessonCompleted -> new LessonMessageListener,
    StatementMessageListener.Destination -> new StatementMessageListener,
    MessageBusDestinations.CourseCompleted -> new CourseMessageListener,
    MessageBusDestinations.TrainingEventCompleted -> new TrainingEventMessageListener,
    MessageBusDestinations.LearningPathEndPoint -> new LearningPathMessageListener
  )

  lazy val messageBusConfigurator = new CustomPluginMessagingConfigurator()

  override def start(context: BundleContext): Unit = {
    //we need to register bundle class loader, without it we will have ClassNotFound in bg tasks
    ClassLoaderPool.register(Configuration.contextName, this.getClass.getClassLoader)

    new SlickActivator().start(context)

    Configuration.messageService.init(MessageListeners.list(dbActions))

    registerMessageListeners()
  }

  override def stop(context: BundleContext): Unit = {
    ClassLoaderPool.unregister(Configuration.contextName)
    messageBusConfigurator.destroy()
    Configuration.messageService.destroy()
  }


  private def registerMessageListeners(): Unit = {
    val listenersMap =
      messageListeners mapValues { listener =>
        val list: java.util.List[MessageListener] = new java.util.ArrayList
        list.add(listener)
        list
      }

    val destinations =
      messageListeners.keys map { destinationName =>
        val destination = new SerialDestination
        destination.setName(destinationName)
        destination.afterPropertiesSet()
        destination.asInstanceOf[Destination]
      } toList

    messageBusConfigurator.setMessageListeners(listenersMap.asJava)
    messageBusConfigurator.setDestinations(destinations.asJava)
    messageBusConfigurator.afterPropertiesSet()

  }
}
