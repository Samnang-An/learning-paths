package com.arcusys.valamis.learningpath.listeners

import com.arcusys.valamis.learningpath.listeners.model.{LPInfo, LPInfoWithUserStatus}
import com.arcusys.valamis.learningpath.models._
import com.arcusys.valamis.learningpath.services.LearningPathService
import com.arcusys.valamis.learningpath.tasks.TaskManager
import com.arcusys.valamis.learningpath.utils.{DbActions, DbActionsSupport}
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}

class LearningPathListener(val dbActions: DbActions,
                           learningPathService: LearningPathService,
                           taskManager: TaskManager,
                           logoPathPrefix: String)
                          (implicit executionContext: ExecutionContext)
  extends DbActionsSupport {

  def getUsersToLPCount(startDate: DateTime, endDate: DateTime, companyId: Long): Future[Map[Long, Int]] = {
    db.run {
      userLPStatusDBIO.getUsersIdsToLPCount(startDate, endDate, companyId)
    } map (x => x.toMap)
  }

  def getLPById(id: Long, companyId: Long): Future[Option[LearningPathWithVersion]] = {
    learningPathService.getById(id)(companyId) map {
      _ map { case (lp, version) =>
        LearningPathWithVersion(lp, version)
      }
    }
  }

  def getLearningPathsByIds(ids: Seq[Long]): Future[Seq[LPInfo]] = {
    learningPathService.getByIds(ids) map {
      _ map { case (lp, version) =>
        LPInfo(
          id = lp.id,
          companyId = lp.companyId,
          activated = lp.activated,
          title = version.title,
          description = version.description,
          logoUrl = version.logo map getLogoUrl
        )
      }
    }
  }

  def getLearningPathWithStatusByIds(userId: Long,
                                     ids: Seq[Long]): Future[Seq[LPInfoWithUserStatus]] = {
    learningPathService.getWithUserStatusByIds(userId, ids) map {
      _ map { case (lp, version, status) =>
        LPInfoWithUserStatus(
          id = lp.id,
          companyId = lp.companyId,
          activated = lp.activated,
          title = version.title,
          validPeriod = version.validPeriod,
          description = version.description,
          logoUrl = version.logo map getLogoUrl,
          status = status.map(_.status),
          statusDate = status.map(_.modifiedDate),
          progress = status.map(_.progress)
        )
      }
    }
  }

  def getPassedLearningPath(userId: Long, companyId: Long): Future[Seq[LPInfoWithUserStatus]] = {
    val filter = LearningPathFilter(
      title = None,
      None,
      None,
      Some(true),
      Some(true)
    )
    learningPathService.getByFilterForMember(filter,
      userId,
      Some(true),
      Some(CertificateStatuses.Success),
      LearningPathSort.title,
      None,
      None
    )(companyId) map {
      _ map { lp =>
        LPInfoWithUserStatus(
          id = lp.learningPath.id,
          companyId = lp.learningPath.companyId,
          activated = lp.learningPath.activated,
          title = lp.versionProperties.title,
          validPeriod = lp.versionProperties.validPeriod,
          description = lp.versionProperties.description,
          logoUrl = lp.versionProperties.logo map getLogoUrl,
          status = lp.userStatus.map(_.status),
          statusDate = lp.userStatus.map(_.modifiedDate),
          progress = lp.userStatus.map(_.progress)
        )
      }
    }
  }

  private def getLogoUrl(logo: String) =
    s"$logoPathPrefix/$logo"
}
