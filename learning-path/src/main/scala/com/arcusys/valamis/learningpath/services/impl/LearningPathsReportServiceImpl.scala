package com.arcusys.valamis.learningpath.services.impl

import com.arcusys.valamis.learningpath.models._
import com.arcusys.valamis.learningpath.models.patternreport._
import com.arcusys.valamis.learningpath.services._
import com.arcusys.valamis.learningpath.services.exceptions.NoLearningPathError
import com.arcusys.valamis.learningpath.utils.{DbActions, DbActionsSupport}
import com.arcusys.valamis.members.picker.service.MemberService
import org.joda.time.DateTime
import slick.dbio.{DBIO, DBIOAction, Effect, NoStream}
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by amikhailov on 23.03.17.
  */
class LearningPathsReportServiceImpl(val dbActions: DbActions,
                                     val learningPathService: LearningPathService,
                                     val goalService: GoalService,
                                     val memberService: MemberService,
                                     val userProgressService: UserProgressService
                                    )(implicit executionContext: ExecutionContext)
  extends LearningPathsReportService
    with DbActionsSupport {

  override def getCertificates(courseId: Option[Long])
                              (implicit companyId: Long): Future[Seq[(LPVersion, GoalsSet)]] = {

    learningPathService.getByFilter(getLearningPathFilter(courseId), LearningPathSort.title, None, 1000)
      .flatMap { learningPaths =>
        Future.sequence(
          learningPaths.map { lp =>
            goalService.getGoalsByLPCurrentVersion(lp._2.learningPathId).map((lp, _))
          }
        )
      }
      .map(_.map {
        case (lp, goals) => (lp._2, goals)
      })
  }

  override def getUsers(courseId: Option[Long], userIds: Seq[Long])
                       (implicit companyId: Long): Future[Seq[(LPVersion, UserLPStatus)]] = {
    val action = for {
      learningPaths <- learningPathDBIO.getByFilter(getLearningPathFilter(courseId), LearningPathSort.title, 0, 1000)
      userStatuses <- DBIO.sequence(learningPaths map { case (lp, lpVersion) =>
        userLPStatusDBIO.getByUserIdsAndLearningPathId(userIds, lp.id) map {
          _.map {
            userLPStatus => (lpVersion, userLPStatus)
          }
        }
      })
    } yield {
      userStatuses.flatten
    }

    db.run(action)
  }

  override def getTotalGoalStatus(learningPathId: Long)
                                 (implicit companyId: Long): Future[Seq[(Long, Seq[(GoalStatuses.Value, Int)])]] = {
    val actions = for {
      lp <- learningPathDBIO.getById(learningPathId)
        .map {
          _.getOrElse(throw new NoLearningPathError(learningPathId))
        }
      goals <- goalDBIO.getByVersionId(
        lp.currentVersionId.getOrElse(throw new NoLearningPathError(learningPathId))
      )
      stat <- DBIO.sequence(
        goals.map {
          goal =>
            userGoalStatusDBIO.getUserGoalStatusesCounts(goal.id)
              .map {
                x => (goal.id, x)
              }
        }
      )
    } yield {
      stat
    }
    db.run(actions)
  }

  override def getTotalStatus(courseId: Option[Long], userIds: Seq[Long])
                             (implicit companyId: Long): Future[Seq[(Long, Seq[(CertificateStatuses.Value, Int)])]] = {
    val maxItemsInSet = 1000
    val actions =
      learningPathDBIO.getByFilter(getLearningPathFilter(courseId), LearningPathSort.title, 0, 1000)
        .flatMap {
          items =>
            DBIO.sequence(items.flatMap {
              case (learningPath, lpVersion) =>
                // we split users into batches per 1000 items to avoid limitation of inSet
                split(userIds, maxItemsInSet).map { uIds =>
                  userLPStatusDBIO.getStatusesCounts(lpVersion.learningPathId, uIds)
                    .map(statuses => (lpVersion.learningPathId, statuses))
                }
            })
        }
    db.run(actions)
  }

  override def getUserGoalStatuses(learningPathId: Long,
                                   userIds: Seq[Long])
                                  (implicit companyId: Long): Future[Seq[Seq[UserGoalStatus]]] = {
    db.run {
      userGoalStatusDBIO.getByUserIdsAndLPId(userIds, learningPathId)
    } map { statuses =>
      statuses.groupBy(_.userId).values.toSeq
    }
  }

  override def getEndDate(certificate: LPVersion,
                          userLPStatus: UserLPStatus): Option[DateTime] = {
    certificate.validPeriod match {
      case None => None
      case Some(interval) => Some(userLPStatus.modifiedDate.plus(interval))
    }
  }

  override def getStatus(certificate: LPVersion,
                         userLPStatus: UserLPStatus): PathsReportStatus.Value = {
    userLPStatus.status match {
      case CertificateStatuses.Failed => PathsReportStatus.Failed
      case CertificateStatuses.InProgress => PathsReportStatus.InProgress
      case CertificateStatuses.Overdue => PathsReportStatus.Expired

      case CertificateStatuses.Success =>
        val endDate = getEndDate(certificate, userLPStatus)
        endDate match {
          case None => PathsReportStatus.Achieved
          case Some(ed) =>
            if (certificate.expiringPeriod.isDefined && ed.minus(certificate.expiringPeriod.get).isBeforeNow) {
              PathsReportStatus.Expiring
            } else {
              PathsReportStatus.Achieved
            }
        }
    }
  }

  override def getJoinedUserIds(courseId: Option[Long])(implicit companyId: Long): Future[Seq[Long]] = {
    db.run {
      courseId match {
        case None => userLPStatusDBIO.getUserIds()
        case Some(course) => userLPStatusDBIO.getUserIds(course)
      }
    }
  }

  private def getLearningPathFilter(courseId: Option[Long]) = {
    LearningPathFilter(None, courseId, None, Some(true), Some(true))
  }

  private def split[A](xs: Seq[A], blockLength: Int): List[Seq[A]] =
    if (xs.isEmpty) Nil
    else (xs take blockLength) :: split(xs drop blockLength, blockLength)
}