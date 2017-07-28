package com.arcusys.valamis.learningpath.listeners.impl

import java.util.UUID

import com.arcusys.valamis.learningpath.listeners.UserLPStatusListener
import com.arcusys.valamis.learningpath.listeners.competences.messages.CompetencesImprovedMessage
import com.arcusys.valamis.learningpath.models.{CertificateActivitesType, LPVersion, LearningPath}
import com.arcusys.valamis.learningpath.services._
import com.arcusys.valamis.learningpath.utils.{DbActions, DbActionsSupport, JsonHelper}
import com.arcusys.valamis.message.broker.MessageService
import org.apache.commons.logging.Log
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by pkornilov on 6/22/17.
  */
class UserLPStatusListenerImpl(val dbActions: DbActions,
                               improvingCompetenceService: CompetenceService,
                               certificateNotificationService: CertificateNotificationService,
                               socialActivityHelper: SocialActivityHelper,
                               lpStatementService: LPStatementService,
                               messageService: MessageService,
                               log: Log)
                              (implicit executionContext: ExecutionContext)
  extends UserLPStatusListener
    with DbActionsSupport {

  import JsonHelper.formats

  private[learningpath] def onCompleted(userId: Long,
                                        lp: LearningPath,
                                        version: LPVersion)
                                       (implicit companyId: Long): Unit = {
    lazy val debugInfo = getDebugInfo(userId, lp, version)

    certificateNotificationService.sendCertificateAchieved(lp.id, Seq(userId))(lp.companyId) onFailure {
      case e => log.error("Failed to send achieved notification" + debugInfo, e)
    }

    Future {
      socialActivityHelper.addWithSet(companyId,
        userId,
        version.courseId,
        classPK = Some(lp.id),
        activityType = Some(CertificateActivitesType.Achieved.id),
        createDate = DateTime.now)
    } onFailure {
      case e => log.error("Failed to add 'Achieved' activity" + debugInfo, e)
    }

    val lpCompletedStatementF = Future {
      lpStatementService.sendStatementCompleted(userId, lp.companyId, version)
    } recover {
      case e =>
        log.error("Failed to send completed statement" + debugInfo, e)
        None
    }

    lpCompletedStatementF flatMap { lpCompletedStatementId =>
      sendImprovedCompetencesMessage(userId, lp, version, lpCompletedStatementId)
    } onFailure {
      case e => log.error("Failed to send improved competences" + debugInfo, e)
    }
  }

  private[learningpath] def onFailed(userId: Long,
                                     lp: LearningPath,
                                     version: LPVersion)
                                    (implicit companyId: Long): Unit = {
    Future {
      socialActivityHelper.addWithSet(companyId,
        userId,
        version.courseId,
        classPK = Some(lp.id),
        activityType = Some(CertificateActivitesType.Failed.id),
        createDate = DateTime.now)
    } onFailure {
      case e =>
        val debugInfo = getDebugInfo(userId, lp, version)
        log.error("Failed to add 'Failed' activity" + debugInfo, e)
    }
  }

  private def sendImprovedCompetencesMessage(userId: Long,
                                             lp: LearningPath,
                                             version: LPVersion,
                                             lpCompletedStatementId: Option[UUID])
                                            (implicit companyId: Long): Future[Unit] = {
    lp.currentVersionId.fold(Future.successful({})) { versionId =>
      improvingCompetenceService.getCompetencesByVersionId(versionId) map { competences =>
        if (competences.nonEmpty) {
          messageService.sendMessage(
            destination = MessageBusDestinations.ImproveCompetencesForUser,
            data = JsonHelper.toJson(
              CompetencesImprovedMessage(
                userId,
                version.title,
                competences,
                lpActivityId = lpStatementService.createActivityId(lp.id, companyId),
                lpCompletedStatementId = lpCompletedStatementId.map(_.toString)
              )
            )
          )
        }
      }
    }
  }

  private def getDebugInfo(userId: Long,
                           lp: LearningPath,
                           version: LPVersion)
                          (implicit companyId: Long) =
    s"\r\ndebugInfo: companyId: $companyId; userId: $userId; lp: $lp, version: $version"
}