import sbt._
import com.typesafe.sbt.osgi.SbtOsgi.autoImport.OsgiKeys
import com.arcusys.sbt.keys._
import com.arcusys.sbt.plugins.CommonDevResourcesPlugin

lazy val commonResources = (project in file("web-resources"))
  .settings(scalaVersion := Version.scala)
  .enablePlugins(CommonDevResourcesPlugin)
  .settings(
    CommonResourcesKeys.devPath := "dev",
    CommonResourcesKeys.configurationPath := "dev/config/config.json",
    CommonResourcesKeys.loaderFilePath := "dist/js/valamis/amd-loader-config.js",
    CommonResourcesKeys.resourcesVersion := Version.valamisCommonResources,
    resolvers ++= Seq(
      Resolver.mavenLocal,
      ArcusysResolvers.mavenCentral,
      ArcusysResolvers.public
    )
  )

val commonSettings = Seq(
  version := Version.current,
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Xfatal-warnings"),
  scalaVersion := Version.scala,
  resolvers ++= Seq(
    Resolver.mavenLocal,
    ArcusysResolvers.public,
    ArcusysResolvers.mavenCentral
  ),
  libraryDependencies ++= Seq(
    Libraries.jodaTime,
    Libraries.jodaConvert,
    Libraries.commonsLogging
  ),
  updateOptions := updateOptions.value.withLatestSnapshots(false),
  CommonKeys.lfVersion := "DELETE ME",
  DeployKeys.lf6Version := "DELETE ME",
  OsgiCommonKeys.lf7Version := "DELETE ME",
  OsgiCommonKeys.osgiHelper := CustomOsgiHelper,
  CommonResourcesKeys.resourcesVersion := Version.valamisCommonResources
)

val commonOsgiSettings = osgiSettings ++ Seq(
  OsgiKeys.requireCapability :=
    """osgi.ee;filter:="(&(osgi.ee=JavaSE)(version=1.7))""""
  // FIXME: incompatible with current osgi helpers.
  // 'consumer-policy' to change import-package version range builder to more strict
  // old ex: slick;version=[3.0, 4)
  // new ex: slick:version=[3.0.3, 3.0.4)
  //  OsgiKeys.additionalHeaders ++= Map(
  //    "-consumer-policy" -> "${range;[===,==+)}"
  //  )
)

/**
  * learning path backend
  */
lazy val learningPath = {
  (project in file("learning-path"))
    .settings(commonSettings)
    .settings(
      name := "learningPath",
      libraryDependencies ++= Seq(
        Libraries.slick,
        Libraries.slickDrivers,
        Libraries.hikari,
        //Libraries.scalaReflect,
        Libraries.slickJodaMapper,
        /*,
        Libraries.valamisSlickBase*/
        Libraries.valamisMembers,
        Libraries.json4sJakson,
        Libraries.json4sExt,
        Libraries.json4sNative,
        Libraries.trainingEventsApi,
        Libraries.valamisMessageBroker,
        Libraries.lrsSupport,
        Libraries.servletApi % Provided
      ),
      libraryDependencies ++= Seq(
        Libraries.scalaTest % Test
      )
    )
    .enablePlugins(SbtOsgi)
    .settings(commonOsgiSettings: _*)
    .settings(
      OsgiKeys.bundleSymbolicName :=
        "com.arcusys.valamis.learningpath.services",
      OsgiKeys.privatePackage ++= Seq(
        "com.arcusys.valamis.learningpath.listeners.competences.messages"
      ),
      OsgiKeys.exportPackage ++= Seq(
        "com.arcusys.valamis.learningpath.listeners.*",
        "com.arcusys.valamis.learningpath.listeners.impl",
        "com.arcusys.valamis.learningpath.models.*",
        "com.arcusys.valamis.learningpath.services.*",
        "com.arcusys.valamis.learningpath.tasks",
        "com.arcusys.valamis.learningpath.serializer",
        "com.arcusys.valamis.learningpath.utils",
        "com.arcusys.valamis.learningpath.messaging.*"
      ),
      OsgiKeys.importPackage := Seq(
        s"""com.arcusys.valamis.members.picker.*;version="${Version.valamisMembers}"""",
        s"""com.arcusys.valamis.training.events.*;version="${Version.trainingEvents}"""",
        "*"
      )
    )
    .enablePlugins(BuildInfoPlugin)
    .settings(
      buildInfoKeys := Seq[BuildInfoKey](version, scalaVersion, sbtVersion),
      buildInfoPackage := "com.arcusys.valamis.util"
    )
}



lazy val migration = {
  (project in file("migration"))
    .settings(commonSettings)
    .settings(
      name := "learningPath-migration",
      libraryDependencies ++= Seq(
        Libraries.slick,
        Libraries.slickMigration,
        Libraries.hikari,
        Libraries.slickJodaMapper,
        Libraries.valamisMembers,
        Libraries.json4sJakson,
        Libraries.json4sExt,
        Libraries.json4sNative,
        Libraries.lrsSupport
      ),
      libraryDependencies ++= Seq(
        Libraries.scalaTest % Test,
        Libraries.h2Driver % Test,
        Libraries.slf4jApi % Test,
        Libraries.logbackCore % Test,
        Libraries.logbackClassic % Test
      )
    )
    .enablePlugins(SbtOsgi)
    .settings(commonOsgiSettings: _*)
    .settings(
      OsgiKeys.bundleSymbolicName :=
        "com.arcusys.valamis.learningpath.migration",
      OsgiKeys.exportPackage ++= Seq(
        "com.arcusys.valamis.learningpath.migration"
      ),
      OsgiKeys.privatePackage ++= Seq(
        "com.arcusys.valamis.learningpath.migration.schema.*"
      ),
      OsgiKeys.importPackage := Seq(
        s"""com.arcusys.valamis.members.picker.*;version="${Version.valamisMembers}"""",
        s"""com.arcusys.slick.migration.*;version="${Version.slickMigration}"""",
        "*"
      )
    )
    .dependsOn(learningPath, slickSupportTest % Test)
}

/**
  * servlet/portlet related code
  * handle query: parse request, call backend and then build response
  */
lazy val web = {
  (project in file("web"))
    .settings(commonSettings)
    .settings(
      name := "learningPath-web",
      libraryDependencies ++= Seq(
        Libraries.portletApi % Provided,
        Libraries.servletApi % Provided,
        Libraries.jspApi % Provided,
        Libraries.mail % Provided
      ),
      libraryDependencies ++= Seq(
        Libraries.scalatraBase,
        Libraries.scalatraAuth,
        Libraries.scalatraJson,
        Libraries.json4sJakson,
        Libraries.json4sExt,
        Libraries.json4sNative,
        Libraries.javaxInject,
        Libraries.lrsSupport
      ),
      libraryDependencies ++= Seq(
        Libraries.scalaTest % Test,
        Libraries.h2Driver % Test,
        Libraries.scalatraTest % Test,
        Libraries.slf4jApi % Test,
        Libraries.logbackCore % Test,
        Libraries.logbackClassic % Test
      )
    )
    .enablePlugins(SbtOsgi)
    .settings(commonOsgiSettings: _*)
    .settings(
      OsgiKeys.bundleSymbolicName :=
        "com.arcusys.valamis.learningpath.web",
      OsgiKeys.exportPackage ++= Seq(
        "com.arcusys.valamis.learningpath.web.*"
      ),
      OsgiKeys.importPackage := Seq(
        s"""com.arcusys.valamis.members.picker.*;version="${Version.valamisMembers}"""",
        "*"
      )
    )
    .dependsOn(learningPath, slickSupportTest % Test)
}

/**
  * liferay 6.2 bundle
  * build war file, register portlets/servlets, call liferay environment
  */
lazy val bundleLR620 = {
  (project in file("liferay6-war-bundle"))
    .enablePlugins(CommonResourcesPlugin)
    .settings(commonSettings)
    .settings(warSettings ++ webappSettings: _*)
    .settings(
      name := "liferay620-bundle",
      artifactName in packageWar := { (_: ScalaVersion, _: ModuleID, artifact: Artifact) =>
        "learning-paths-portlet." + artifact.extension
      },
      libraryDependencies ++= Dependencies.liferay6.map(_ % Provided) ++
        Dependencies.trainingEventService ++
        Seq(Libraries.lfCalendar620,
          Libraries.valamisSettings, Libraries.lrsSupport,
          Libraries.valamisMessageBroker, Libraries.valamisMessageBrokerLR620)
    )
    .settings(postProcess in webapp := { webAppDir =>

      val srcDir = webAppDir / "/../../../web-resources/src/main/resources"

      IO.copyDirectory(srcDir, webAppDir, preserveLastModified = true, overwrite = true)

    })
    .settings(CommonResourcesKeys.configurationPath := "../web-resources/dev/config/config.json")
    .enablePlugins(DeployPlugin)
    .dependsOn(web, migration)
}

/**
  * liferay 7 bundle
  * build main jar file with activator, register portlets/servlets, call liferay environment
  */
lazy val bundleLR700 = {
  (project in file("liferay7-jar-bundle"))
    .enablePlugins(OsgiCommonResourcesPlugin)
    .settings(commonSettings)
    .settings(
      name := "learningPath-portlets",
      artifactName := { (_: ScalaVersion, module: ModuleID, artifact: Artifact) =>
        "learning-paths-portlet-" + module.revision + "." + artifact.extension
      },
      libraryDependencies ++=
        Dependencies.liferay7.map(_ % Provided) ++
          Dependencies.liferay7Plugins.map(_ % Provided) ++
          Dependencies.trainingEventService ++
          Seq(Libraries.valamisSettings, Libraries.valamisMessageBroker, Libraries.valamisMessageBrokerLR700)
    )
    .enablePlugins(SbtOsgi)
    .enablePlugins(OsgiDependenciesPlugin, OsgiMainPlugin)
    .settings(commonOsgiSettings: _*)
    .settings(
      libraryDependencies ++=
        Dependencies.osgi.map(_ % Provided),
      OsgiKeys.bundleSymbolicName :=
        "com.arcusys.valamis.learningpath.portlets",
      OsgiKeys.additionalHeaders ++= Map(
        "-dsannotations" -> "*",
        "Service-Component" -> "VALAMIS-OSGI-INF/*",
        "Liferay-JS-Config" -> "META-INF/resources/js/valamis/amd-loader-config.js"

      ),
      OsgiKeys.bundleActivator := Some(
        "com.arcusys.valamis.learningpath.init.Activator"
      ),
      OsgiKeys.privatePackage ++= Seq(
        "com.arcusys.valamis.learningpath.init",
        "com.arcusys.valamis.learningpath.init.language",
        "com.arcusys.valamis.learningpath.interpreter",
        "com.arcusys.valamis.learningpath.impl.*",
        "com.arcusys.valamis.learningpath.servlets",
        "com.arcusys.valamis.learningpath.portlets",
        "com.arcusys.valamis.learningpath.listeners.liferay",
        "com.arcusys.valamis.learningpath.strutsactions",
        "com.arcusys.valamis.learningpath.handler",
        "com.arcusys.valamis.learningpath.init.notification",
        "com.arcusys.valamis.learningpath.asset",
        "com.arcusys.valamis.learningpath.schedulers"
      ),
      OsgiKeys.importPackage := Seq(
        s"""com.arcusys.valamis.members.picker.*;version="${Version.valamisMembers}"""",
        "*"
      )
    )
    .settings(resourceGenerators in Compile <+=
      (resourceManaged in Compile, name, version) map { (dir, _, _) =>

        val sourceDir = dir / "/../../../../../web-resources/src/main/resources"
        val targetDir = dir / "META-INF/resources"

        IO.createDirectory(targetDir)
        IO.copyDirectory(sourceDir, targetDir, preserveLastModified = true, overwrite = true)

        Seq[File]()
      }
    )
    .settings(CommonResourcesKeys.configurationPath := "../web-resources/dev/config/config.json")
    .settings(CommonResourcesKeys.depsFilePath := "/VALAMIS-OSGI-INF/portlet.LearningPathsPortlet.xml")
    .dependsOn(web, migration)
}

lazy val slickSupportTest = {
  project
    .in(file("slick-test"))
    .settings(commonSettings: _*)
    .settings(name := "valamis-slick-test")
    .settings(libraryDependencies ++= {
      Seq(Libraries.slick, Libraries.slickDrivers, Libraries.h2Driver)
    })
}