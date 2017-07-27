import java.util.jar.Attributes

import aQute.bnd.osgi.Constants
import aQute.lib.utf8properties.UTF8Properties
import com.arcusys.sbt.utils.OsgiHelper
import sbt._

object CustomOsgiHelper extends OsgiHelper {
  override val osgiBundlesZip: String = "LearningPathBundles.zip"
  override val osgiDepsZip: String = "LearningPathDependencies.zip"
  override val lpkgName: String = "LearningPathOnly"


  //structure of this map:
  //libFileName -> (Host Bundle-SymbolicName,Fragment Bundle-SymbolicName)
  override protected val fragments = Map(
    //fragments of scalatra_2.11-2.3.1.jar (scalatra)
    s"scalatra-common_2.11-${Version.scalatra}.jar" -> ("scalatra", "scalatra-common"),
    //scalatra-auth, scalatra-json have their own package names and don't have to be a fragment

    //fragments of json4s-core_2.11-3.2.11.jar (json4s-core)
    s"json4s-ast_2.11-${Version.json4s}.jar" -> ("json4s-core", "json4s-ast")
    //json4s-ext has its own package name and don't have to be a fragment
  )
  override val customOsgiExport = Map(
    "javax.json-1.0.4.jar" -> (
      "javax.json;version=\"" + jsonVersion + "\"," +
        "javax.json.spi;version=\"" + jsonVersion + "\"," +
        "javax.json.stream;version=\"" + jsonVersion + "\",*"
      )
  )
  override protected val customExport = Map(
    s"slick-drivers_2.11-${Version.slickDrivers}.jar" -> s"""com.arcusys.slick.drivers.*;version="${Version.slickDrivers}"""",
    s"slick-migration_2.11-${Version.slickMigration}.jar" -> s"""com.arcusys.slick.migration.*;version="${Version.slickMigration}"""",
    s"slick-migration_2.11.jar" -> s"""com.arcusys.slick.migration.*;version="${Version.slickMigration}""""//for publishLocal
  )
  // Without import correction (change version, remove) the OSHI library (oshi-core-2.6-m-java7.jar)
  // can not found json classes if the javax.json deployed before OSHI.
  override val customOsgiImport = Map(
    "javax.json-1.0.4.jar" -> (
      "javax.json;version=\"" + jsonVersion + "\";resolution:=optional," +
        "javax.json.spi;version=\"" + jsonVersion + "\";resolution:=optional," +
        "javax.json.stream;version=\"" + jsonVersion + "\";resolution:=optional,*"
      )
  )
  //com.sun.management, com.sun.tools.javadoc are available through bootdelegation mechanism
  //and should NOT be imported explicitly
  //see also: http://spring.io/blog/2009/01/19/exposing-the-boot-classpath-in-osgi/
  override protected val customImport = Map(
    "oshi-core-2.6-m-java7.jar" -> ("!com.sun.management,javax.json;version=\"" + jsonVersion + "\",*"),
    s"slick-migration_2.11-${Version.slickMigration}.jar" ->
      s"""
         |slick.*;version="[${Version.slick},3.1)",
         |com.arcusys.slick.drivers.*;version="[${Version.slickDrivers},3.1)",*""".stripMargin
  )
  //for some reason collectDependencies task produce some unnecessary(in OSGi runtime) dependencies
  //so here we manually exclude them
  override val exceptions: ModuleFilter = moduleFilter() - (//all modules except those listed below
    "ch.qos.logback" % "logback-classic" |
      "ch.qos.logback" % "logback-core" |

      "org.slf4j" % "slf4j-api" |
      "org.slf4j" % "slf4j-simple" |
      "org.slf4j" % "jcl-over-slf4j"
    )

  // Original exported version is hidden by Liferay Portal Remote CXF Common (2.0.5, Liferay 7.0 GA2)
  // without providing access to classes implementation.
  // Set other version to access classes by OSHI library.
  private val jsonVersion = "1.0.41"

  override protected def fixVersion(attributes: Option[Attributes])(implicit props: UTF8Properties): Unit = {

    attributes.foreach { attrs =>
      Option(attrs.getValue("Implementation-Version"))
        .map(_.replace("-SNAPSHOT", ".SNAPSHOT"))
        .foreach(props.put(Constants.BUNDLE_VERSION, _))
    }
  }
}