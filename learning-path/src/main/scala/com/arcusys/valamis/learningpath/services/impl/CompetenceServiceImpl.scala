package com.arcusys.valamis.learningpath.services.impl

import com.arcusys.valamis.learningpath.models.{Competence, CompetenceDbEntity}
import com.arcusys.valamis.learningpath.services.CompetenceService
import com.arcusys.valamis.learningpath.services.exceptions.{NoLearningPathDraftError, NoLearningPathError, NoVersionError}
import com.arcusys.valamis.learningpath.services.impl.actions.BaseCompetenceDBIOActions
import com.arcusys.valamis.learningpath.utils.{DbActions, DbActionsSupport}
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by pkornilov on 6/6/17.
  */
class CompetenceServiceImpl(val dbActions: DbActions,
                            competenceDBIO: => BaseCompetenceDBIOActions)
                           (implicit val executionContext: ExecutionContext) extends CompetenceService
  with DbActionsSupport {

  import profile.api._

  override def getCompetencesByVersionId(versionId: Long)
                                        (implicit companyId: Long): Future[Seq[Competence]] =
    db.run {
      for {
        version <- versionDBIO.getById(versionId)
        res <- version match {
          case Some(_) => competenceDBIO.getByVersionId(versionId)
          case None => DBIO.failed(new NoVersionError(versionId))
        }
      } yield res
    }

  override def getCompetencesForLPLastDraft(learningPathId: Long)
                                           (implicit companyId: Long): Future[Seq[Competence]] = {
    db.run(checkLpExistence(learningPathId) >> versionDBIO.getDraftByLearningPathId(learningPathId))
      .map(_.getOrElse(throw new NoLearningPathDraftError(learningPathId)))
      .flatMap { case (versionId, _) =>
        getCompetencesByVersionId(versionId)
      }
  }

  override def getCompetencesForLPCurrentVersion(learningPathId: Long)
                                                (implicit companyId: Long): Future[Seq[Competence]] = {
    db.run(learningPathDBIO.getById(learningPathId))
      .map(_.getOrElse(throw new NoLearningPathError(learningPathId)))
      .flatMap { learningPath =>
        getCompetencesByVersionId(learningPath.currentVersionId.get)
      }
  }

  override def create(learningPathId: Long,
                      competence: Competence)
                     (implicit companyId: Long): Future[Unit] = {
    //TODO check for competence existence?
    performActionOnLPDraft(learningPathId) { versionId =>
      competenceDBIO.insert(new CompetenceDbEntity(versionId, competence))
    }
  }

  override def delete(learningPathId: Long,
                      skillId: Long)
                     (implicit companyId: Long): Future[Unit] =
    performActionOnLPDraft(learningPathId) { versionId =>
      competenceDBIO.delete(versionId, skillId) map (_ => ())
    }

  override def updateSkillName(skillId: Long,
                               skillName: String)
                              (implicit companyId: Long): Future[Unit] = db.run {
    competenceDBIO.updateSkillName(skillId, skillName) map (_ => ())
  }

  override def updateLevelName(levelId: Long,
                               levelName: String)
                              (implicit companyId: Long): Future[Unit] = db.run {
    competenceDBIO.updateLevelName(levelId, levelName) map (_ => ())
  }

  private def performActionOnLPDraft[T](learningPathId: Long)
                                       (action: Long => DBIO[T])
                                       (implicit companyId: Long): Future[Unit] = {
    val resAction = for {
      _ <- checkLpExistence(learningPathId)
      (versionId, _) <- versionDBIO.getDraftByLearningPathId(learningPathId)
        .map(_.getOrElse(throw new NoLearningPathDraftError(learningPathId)))
      _ <- action(versionId)
      _ <- versionDBIO.updateModifiedDate(versionId, DateTime.now())
    } yield ()

    db.run(resAction.transactionally)
  }

  private def checkLpExistence(learningPathId: Long)(implicit companyId: Long) =
    learningPathDBIO.getById(learningPathId) flatMap {
      case None => DBIO.failed(new NoLearningPathError(learningPathId))
      case Some(_) => DBIO.successful({})
    }

}
