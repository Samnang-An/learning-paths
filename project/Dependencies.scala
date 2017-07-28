import sbt._

object Version {
  val current = "1.1.1"

  //versions of modules developed/forked by Arcusys
  val valamisSettings = "3.4.0"
  val valamisLrsSupport = "1.1.3"
  val valamisMembers = "1.2.0"
  val trainingEvents    = "1.2.4"
  val valamisMessageBroker = "1.0.0"

  val slickDrivers = "3.0.3"
  val slickMigration = "3.0.7"

  val valamisCommonResources = "1.2.0"

  //third party libraries versions
  val scala = "2.11.8"
  val scalaTest = "2.2.3"

  val jodaTime = "2.9.7"
  val jodaConvert = "1.8.1"

  val commonsIO = "2.4"
  val commonsLogging = "1.1.3"

  val slf4j = "1.7.20"
  val logback = "1.0.7"

  val portletApi = "2.0"
  val servletApi = "2.5"
  val jspApi = "2.1"
  val javaxMail = "1.4"
  val javaxInject = "1"

  val scalatra = "2.3.1" //"2.4.1" // 2.4.x - last versions with java 7 support
  val json4s = "3.2.11" //"3.3.0"

  val liferay620 = "6.2.5"
  val liferay7 = "7.0.0"
  val liferay7Plugins = "2.3.0"
  val liferay7Utils = "2.0.1"
  val liferay7BackgroundApi = "2.1.0"
  val liferay7JournalApi = "2.4.0"
  val liferay7DDMApi = "3.5.4"
  val liferayRegistryApi = "1.1.0"
  val liferay7BookmarksApi = "2.0.0"
  val liferay7WikiApi = "2.2.0"

  val slick = "3.0.3"// "3.1.1"
  val hikari = "2.3.7"
  val slickJodaMapperVersion = "2.2.0"
  val h2 = "1.3.170"

  val httpClient        = "4.4"

  val liferay620Calendar   = "6.2.0.13"
  val liferayCalendarApi  = "2.0.1"
}

object Libraries {
  val jodaTime = "joda-time" % "joda-time" % Version.jodaTime
  val jodaConvert = "org.joda" % "joda-convert" % Version.jodaConvert

  val commonsIO = "commons-io" % "commons-io" % Version.commonsIO
  val commonsLogging = "commons-logging" % "commons-logging" % Version.commonsLogging

  val slick = "com.typesafe.slick" %% "slick" % Version.slick
  // hidden dependency slick 3.0.3-> com.zaxxer » HikariCP-java6
  val hikari = "com.zaxxer" % "HikariCP-java6" % Version.hikari
  // hidden dependency: slick 3.1.1 -> scala-reflect
  val scalaReflect = "org.scala-lang" % "scala-reflect" % Version.scala

  val slickDrivers = "com.arcusys.slick" %% "slick-drivers" % Version.slickDrivers
  val slickMigration = "com.arcusys.slick" %% "slick-migration" % Version.slickMigration
  // slickDrivers -> resource
  val scalaARM = "com.jsuereth" %% "scala-arm" % "1.4"

  val slickJodaMapper = "com.github.tototoshi" %% "slick-joda-mapper" % Version.slickJodaMapperVersion

  val h2Driver = "com.h2database" % "h2" % Version.h2


  val scalaTest = "org.scalatest" %% "scalatest" % Version.scalaTest

  val slf4jApi = "org.slf4j" % "slf4j-api" % Version.slf4j
  val logbackCore = "ch.qos.logback" % "logback-core" % Version.logback
  val logbackClassic = "ch.qos.logback" % "logback-classic" % Version.logback


  val portletApi = "javax.portlet" % "portlet-api" % Version.portletApi
  val servletApi = "javax.servlet" % "servlet-api" % Version.servletApi
  val jspApi = "javax.servlet.jsp" % "jsp-api" % Version.jspApi
  val mail = "javax.mail" % "mail" % Version.javaxMail

  val scalatraBase = "org.scalatra" %% "scalatra" % Version.scalatra
  val scalatraAuth = "org.scalatra" %% "scalatra-auth" % Version.scalatra
  val scalatraJson = "org.scalatra" %% "scalatra-json" % Version.scalatra
  val scalatraTest = "org.scalatra" %% "scalatra-scalatest" % Version.scalatra

  val json4sJakson = "org.json4s" %% "json4s-jackson" % Version.json4s
  val json4sExt = "org.json4s" %% "json4s-ext" % Version.json4s
  val json4sNative = "org.json4s" % "json4s-native_2.11" % Version.json4s
  // json4s-core 3.2.11 ->  javax.inject
  val javaxInject = "javax.inject" % "javax.inject" % Version.javaxInject

  val lfPortalService620 = "com.liferay.portal" % "portal-service" % Version.liferay620
  val lfPortalImpl620 = "com.liferay.portal" % "portal-impl" % Version.liferay620
  val lfUtilJava620 = "com.liferay.portal" % "util-java" % Version.liferay620
  val lfCalendar620      = "com.liferay.calendar" % "calendar-portlet-service" % Version.liferay620Calendar

  val lfPortalService700  = "com.liferay.portal" % "com.liferay.portal.kernel" % Version.liferay7Plugins
  val lfPortalImpl700     = "com.liferay.portal" % "com.liferay.portal.impl"   % Version.liferay7Plugins
  val lfUtilJava700       = "com.liferay.portal" % "com.liferay.util.java"     % Version.liferay7Utils
  val lfBackgroundTask700 = "com.liferay" % "com.liferay.portal.background.task.api" % Version.liferay7BackgroundApi
  val lfJournal700        = "com.liferay" % "com.liferay.journal.api"                % Version.liferay7JournalApi
  val lfDDM700            = "com.liferay" % "com.liferay.dynamic.data.mapping.api"   % Version.liferay7DDMApi
  val lfRegistry700       = "com.liferay" % "com.liferay.registry.api"               % Version.liferayRegistryApi
  val lfWikiApi700        = "com.liferay" % "com.liferay.wiki.api"                   % Version.liferay7WikiApi
  val lfBookmarks700      = "com.liferay" % "com.liferay.bookmarks.api"              % Version.liferay7BookmarksApi
  val lfCalendar700       = "com.liferay" % "com.liferay.calendar.api"               % Version.liferayCalendarApi

  val osgiAnnotation = "org.osgi" % "org.osgi.annotation" % "6.0.0"
  val osgiCompendium = "org.osgi" % "org.osgi.compendium" % "5.0.0"
  val osgiCore = "org.osgi" % "org.osgi.core" % "5.0.0"
  val osgiWhiteboard = "org.osgi" % "org.osgi.service.http.whiteboard" % "1.0.0"

  val valamisSettings   = "com.arcusys.valamis" %% "valamis-settings" % Version.valamisSettings
  val valamisMembers   = "com.arcusys.valamis" %% "valamis-member-picker" % Version.valamisMembers
  val valamisMessageBroker = "com.arcusys.valamis" %% "valamis-message-broker" % Version.valamisMessageBroker
  val valamisMessageBrokerLR620 = "com.arcusys.valamis" %% "valamis-message-broker-liferay620" % Version.valamisMessageBroker
  val valamisMessageBrokerLR700 = "com.arcusys.valamis" %% "valamis-message-broker-liferay700" % Version.valamisMessageBroker

  val lrsSupport = "com.arcusys.valamis" %% "valamis-lrssupport" % Version.valamisLrsSupport

  val trainingEventsApi = "com.arcusys.valamis" %% "valamis-training-events-api" % Version.trainingEvents
  val trainingEventsService = "com.arcusys.valamis" %% "valamis-training-events-service" % Version.trainingEvents

  val httpClient = "org.apache.httpcomponents" % "httpclient"      % Version.httpClient

}

object Dependencies {

  import Libraries._

  val osgi = Seq(osgiAnnotation, osgiCompendium, osgiCore, osgiWhiteboard)

  val liferay6 = Seq(portletApi, servletApi, jspApi, mail,
    lfPortalService620, lfPortalImpl620, lfUtilJava620)

  val liferay7 = Seq(portletApi, servletApi, jspApi, mail,
    lfPortalService700, lfPortalImpl700, lfUtilJava700)

  val liferay7Plugins = Seq(lfBackgroundTask700, lfJournal700, lfDDM700,
    lfRegistry700, lfWikiApi700, lfBookmarks700, lfCalendar700)

  val trainingEventService = Seq(Libraries.trainingEventsApi, Libraries.trainingEventsService)
}
