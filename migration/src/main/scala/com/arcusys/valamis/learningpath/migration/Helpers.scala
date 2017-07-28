package com.arcusys.valamis.learningpath.migration

import java.io.ByteArrayInputStream
import java.util.UUID

import com.arcusys.valamis.learningpath.migration.schema.old.model.{Certificate, FileRecord}
import com.arcusys.valamis.learningpath.services.{AssetEntryService, CompanyService, FileStorage}
import org.apache.commons.logging.Log

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by pkornilov on 3/24/17.
  */
trait Helpers {

  def companyService: CompanyService

  def assetEntryService: AssetEntryService

  def fileStorage: FileStorage

  def log: Log

  implicit def executionContext: ExecutionContext

  private[migration] def saveLogoFile(fileRecord: Option[FileRecord])
                                     (implicit companyId: Long): Future[Option[String]] = {
    fileRecord match {
      case Some(record) =>
        record.content match {
          case Some(data) =>
            val extensionPostFix = if (record.filename.contains(".")) {
              "." + record.filename.split('.').last
            } else {
              //files from media gallery can don't have extension in its name
              "" //TODO what extension to use in this case?
            }
            val newLogo = UUID.randomUUID().toString + extensionPostFix
            fileStorage.add(newLogo, new ByteArrayInputStream(data)) map (_ => Some(newLogo))
          case _ => Future.successful(None)
        }
      case None => Future.successful(None)
    }
  }

  private[migration] def findCreatedUsedId(cert: Certificate): Long = {
    val assetEntryUserId = try {
      assetEntryService.getAssetEntryUserId(oldCertificateClassName, cert.id)
    } catch {
      case _: Throwable =>
        log.info(s"Unable to find assetEntry for $cert - service is unavailable.")
        None
    }
    assetEntryUserId getOrElse companyService.getCompanyDefaultUserId(cert.companyId)
  }

  private val oldCertificateClassName = "com.arcusys.valamis.certificate.model.Certificate"

}
