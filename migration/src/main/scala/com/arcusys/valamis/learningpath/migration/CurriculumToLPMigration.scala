package com.arcusys.valamis.learningpath.migration

import com.arcusys.valamis.learningpath.services.impl.tables.TableManager
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickDBInfo
import com.arcusys.valamis.learningpath.services.{AssetEntryService, CompanyService, FileStorage}
import com.arcusys.valamis.learningpath.utils.DbActions
import com.arcusys.valamis.members.picker.model.MemberTypes
import com.arcusys.valamis.members.picker.service.LiferayHelper
import org.apache.commons.logging.Log

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

/**
  * Created by pkornilov on 3/27/17.
  */

abstract class CurriculumToLPMigration(dbInfo: SlickDBInfo,
                                       dbActions: DbActions,
                                       liferay: LiferayHelper,
                                       companyService: CompanyService,
                                       assetEntryService: AssetEntryService,
                                       fileStorage: FileStorage,
                                       log: Log)
                                      (implicit val executionContext: ExecutionContext) {
  self =>

  def isMemberExisted(memberId: Long, memberType: MemberTypes.Value): Boolean

  import dbInfo.profile.api._

  def run(throwEx: Boolean = false): Unit = {

    try {
      val tableManager = new TableManager(dbInfo.db, dbInfo.profile, Some(log))
      lazy val dataMigrator = new DataMigrator(dbActions, liferay,
        companyService, assetEntryService, fileStorage, log) {
        override def isMemberExisted(memberId: Long, memberType: MemberTypes.Value): Boolean = {
          self.isMemberExisted(memberId, memberType)
        }
      }

      log.info("Running LP migration...")
      val action = tableManager.hasTables() flatMap {
        case false =>
          log.info("No LP tables are found.")
          for {
            _ <- tableManager.createTables(withAutInc = false)
            _ <- dataMigrator.migrateOldData()
          } yield ()
        case _ =>
          log.info("Migration from Curriculum to Learning Path has been completed earlier.")
          slick.dbio.DBIO.successful({})
      }


      Await.result(dbInfo.db.run(action.transactionally), Duration.Inf)
      log.info("Migration from Curriculum to Learning Path has been completed!")
    } catch {
      case ex: Throwable =>
        log.error("Failed to create tables or migrate old data", ex)
        if (throwEx) {
          throw ex
        }
    }
  }
}
