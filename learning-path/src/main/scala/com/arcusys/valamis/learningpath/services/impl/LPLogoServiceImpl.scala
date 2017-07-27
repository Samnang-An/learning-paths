package com.arcusys.valamis.learningpath.services.impl

import java.io.InputStream
import java.util.UUID

import com.arcusys.valamis.learningpath.services.exceptions.NoLearningPathDraftError
import com.arcusys.valamis.learningpath.services.{FileStorage, LPLogoService}
import com.arcusys.valamis.learningpath.utils.{DbActions, DbActionsSupport}
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by mminin on 26/01/2017.
  */
class LPLogoServiceImpl(val dbActions: DbActions,
                        logoFileStorage: FileStorage)
                       (implicit executionContext: ExecutionContext)
  extends LPLogoService
    with DbActionsSupport {

  import profile.api._

  /**
    * remove logo if not used in any versions
    */
  private def deleteUnusedLogoFile(logo: String)
                                  (implicit companyId: Long): Future[Unit] = {
    db.run(versionDBIO.getCountByLogo(logo)) flatMap {
      case v if v > 0 => Future.successful(Unit)
      case _ => logoFileStorage.delete(logo)
    }
  }

  def setDraftLogo(id: Long, fileName: String, stream: InputStream)
                  (implicit companyId: Long): Future[String] = {
    val extension = fileName.split('.').last
    val newLogo = UUID.randomUUID().toString + "." + extension

    for {
      (versionId, oldVersion) <- db.run(versionDBIO.getDraftByLearningPathId(id))
        .map(_.getOrElse(throw new NoLearningPathDraftError(id)))

      _ <- logoFileStorage.add(newLogo, stream)
      _ <- db.run {
        versionDBIO.updateLogoAndModifiedDate(versionId, Some(newLogo), DateTime.now())
          .transactionally
      }
      _ <- oldVersion.logo match {
        case None => Future.successful(Unit)
        case Some(oldLogo) => deleteUnusedLogoFile(oldLogo)
      }
    } yield {
      newLogo
    }
  }

  def deleteDraftLogo(id: Long)
                     (implicit companyId: Long): Future[Unit] = {

    db.run(versionDBIO.getDraftByLearningPathId(id))
      .map { _.getOrElse(throw new NoLearningPathDraftError(id)) }
      .map { case (versionId, version) => (versionId, version.logo) }
      .flatMap {
        case (versionId, None) =>
          Future.successful(Unit)
        case (versionId, Some(logo)) => {
          db.run {
            versionDBIO.updateLogoAndModifiedDate(versionId, None, DateTime.now())
              .transactionally
          } flatMap { _ =>
            deleteUnusedLogoFile(logo)
          }
        }
      }
  }

  def getLogo(logoName: String)
             (implicit companyId: Long): Future[Option[InputStream]] = {
    logoFileStorage.get(logoName)
  }
}
