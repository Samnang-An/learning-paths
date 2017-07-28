package com.arcusys.valamis.learningpath

import javax.servlet.{ServletContextEvent, ServletContextListener}

import com.arcusys.valamis.learningpath.listeners._
import com.arcusys.valamis.learningpath.migration.{CompetencesMigration, CurriculumToLPMigration, HistoryTablesMigration}
import com.arcusys.valamis.learningpath.services.MessageBusDestinations
import com.arcusys.valamis.learningpath.utils.LiferayLogSupport
import com.arcusys.valamis.members.picker.model.MemberTypes
import com.liferay.portal.kernel.messaging.config.PluginMessagingConfigurator
import com.liferay.portal.kernel.messaging.{Destination, MessageListener, SerialDestination}
import com.liferay.portal.service.{OrganizationLocalServiceUtil, RoleLocalServiceUtil, UserGroupLocalServiceUtil, UserLocalServiceUtil}

import scala.collection.JavaConverters._
import scala.language.postfixOps

/**
  * Created by mminin on 07/02/2017.
  */
class ContextListener
  extends ServletContextListener
    with LiferayLogSupport { self =>

  import Configuration.{executionContext, dbActions}

  private val messageListeners = Map(
    MessageBusDestinations.AssignmentCompleted -> new AssignmentMessageListener,
    MessageBusDestinations.LessonCompleted -> new LessonMessageListener,
    StatementMessageListener.Destination -> new StatementMessageListener,
    MessageBusDestinations.CourseCompleted -> new CourseMessageListener,
    MessageBusDestinations.TrainingEventCompleted -> new TrainingEventMessageListener,
    MessageBusDestinations.LearningPathEndPoint -> new LearningPathMessageListener
  )

  lazy val messageBusConfigurator = new PluginMessagingConfigurator()

  private def isMemberExisted(memberId: Long, memberType: MemberTypes.Value): Boolean = {
    val member = memberType match {
      case MemberTypes.User => UserLocalServiceUtil.fetchUser(memberId)
      case MemberTypes.Role => RoleLocalServiceUtil.fetchRole(memberId)
      case MemberTypes.UserGroup => UserGroupLocalServiceUtil.fetchUserGroup(memberId)
      case MemberTypes.Organization => OrganizationLocalServiceUtil.fetchOrganization(memberId)
    }
    Option(member).isDefined
  }

  override def contextInitialized(sce: ServletContextEvent): Unit = {

    new CurriculumToLPMigration(
      Configuration.dbInfo, Configuration.dbActions,
      Configuration.liferayHelper, Configuration.companyService,
      Configuration.assetEntryService,
      Configuration.logoFileStorage, log) {
      override def isMemberExisted(memberId: Long, memberType: MemberTypes.Value): Boolean = {
        self.isMemberExisted(memberId, memberType)
      }
    }
      .run()

    new HistoryTablesMigration(Configuration.dbInfo, log).run()

    new CompetencesMigration(Configuration.dbInfo, log).run()

    registerMessageListeners()

    Configuration.messageService.init(MessageListeners.list(dbActions))
  }

  override def contextDestroyed(sce: ServletContextEvent): Unit = {
    //TODO: try to shutdown executionContext to avoid error on shutdown:
    //   java.lang.NoClassDefFoundError: scala/concurrent/forkjoin/ForkJoinPool$EmptyTask

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
