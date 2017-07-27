package com.arcusys.valamis.learningpath.web.servlets

import com.arcusys.valamis.learningpath.models.{CertificateStatuses, _}
import com.arcusys.valamis.learningpath.models.patternreport.{PathReportResult, SkipTake}
import com.arcusys.valamis.learningpath.services.{UserService, _}
import com.arcusys.valamis.learningpath.web.servlets.base.LearningPathServletBase
import com.arcusys.valamis.learningpath.web.servlets.response.patternreport._
import com.arcusys.valamis.members.picker.service.{LiferayHelper, MemberService}
import org.scalatra.NotFound
import com.arcusys.valamis.learningpath.models.patternreport.PathsReportStatus

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

trait LearningPatternReportServlet {
  self: LearningPathServletBase =>

  implicit val executionContext: ExecutionContext

  protected val learningPatternReportPrefix: String

  def courseId: Long = params.getAs[Long]("courseId") getOrElse halt(NotFound())

  def courseIdOpt: Option[Long] = params.getAs[Long]("courseId").orElse {
    if (params("courseId") == "all") None else halt(NotFound())
  }

  def namePattern: Option[String] = params.get("filter").filterNot(_.isEmpty)

  protected def patternReportService: LearningPathsReportService

  protected def learningPathService: LearningPathService

  protected def goalService: GoalService

  protected def lrActivityTypeService: LRActivityTypeService

  protected def lessonService: LessonService

  def liferayHelper: LiferayHelper

  protected def memberService: MemberService

  def userService: UserService

  protected def userProgressService: UserProgressService

  get(s"$learningPatternReportPrefix/course/:courseId/certificate/?")(await {

    val cId = companyId
    val goalsAndLearningPathsF = patternReportService.getCertificates(courseIdOpt)(cId)

    for {
      goalsAndPaths <- goalsAndLearningPathsF
      activities <- lrActivityTypeService.getAll(cId)
    } yield {
      val activitiesMap = activities map {
        activity => (activity.activityName, activity.title)
      } toMap

      goalsAndPaths.map {
        case (lpVersion, goals) =>
          val goalsResponse: Seq[BaseGoalPathsResponse] =
            goals.lrActivities.map {
              case (goal, goalLRActivity) =>
                val activityTitle = activitiesMap.get(goalLRActivity.activityName) match {
                  case None => goalLRActivity.activityName
                  case Some(title) => s"${goalLRActivity.count} ${title}"
                }
                new ActivityGoalPathsResponse(goal, goalLRActivity, activityTitle)
            } ++ goals.lessons.map {
              case (goal, goalLesson, str) =>
                new LessonGoalPathsResponse(goal, goalLesson, str)
            } ++ goals.assignments.map {
              case (goal, goalAssignment, title) =>
                new AssignmentGoalPathsResponse(goal, goalAssignment, title)
            } ++ goals.courses.map {
              case (goal, goalCourse, title) =>
                new CourseGoalPathsResponse(goal, goalCourse, title)
            } ++ goals.trainingEvents.map {
              case (goal, goalTrainingEvent, title) =>
                new EventGoalPathsResponse(goal, goalTrainingEvent, title)
            } ++ goals.statements.map {
              case (goal, goalStatement) =>
                new StatementGoalPathsResponse(goal, goalStatement, goalStatement.verbId + " " + goalStatement.objectId)
            } ++ goals.webContents.map {
              case (goal, goalStatement, title) =>
                new WebContentGoalPathsResponse(goal, goalStatement, title)
            }

          Map("title" -> lpVersion.title,
            "id" -> lpVersion.learningPathId,
            "creationDate" -> lpVersion.createdDate,
            "goals" -> goalsResponse)
      }
    }
  })

  get(s"$learningPatternReportPrefix/course/:courseId/users/?")(await {
    val course = courseIdOpt
    val cId = companyId

    for {
      userIds <- getUserIds(courseIdOpt, namePattern, Some(skipTake))(cId)
      userStatus <- patternReportService.getUsers(course, userIds)(cId)
    } yield {
      val userStatuses = userStatus map {
        case (lpVersion, userStatus) =>
          new UserCertificateStatusResponse(
            lpVersion.learningPathId,
            userStatus.userId,
            patternReportService.getStatus(lpVersion, userStatus).id
          )
      }

      val users = userIds.map {
        userId => liferayHelper.getUserInfo(userId, Nil)
      }

      users map { userInfo =>
        Map(
          "id" -> userInfo.id,
          "user" -> Map(
            "id" -> userInfo.id,
            "name" -> userInfo.name,
            "picture" -> userInfo.logo
          ),
          "organizations" -> userInfo.organizations.map(org => org.name),
          "certificates" -> userStatuses.filter(x => x.userId == userInfo.id)
        )
      }
    }
  })

  get(s"$learningPatternReportPrefix/course/:courseId/usersCount/?")(await {
    val cId = companyId
    for {
      userIds <- getUserIds(courseIdOpt, namePattern, None)(cId)
    } yield {
      Map("result" -> userIds.length)
    }
  })

  get(s"$learningPatternReportPrefix/course/:courseId/total/?")(await {
    val activeUserIds = userService.search(namePattern, None)

    for {
      items <- patternReportService.getTotalStatus(courseIdOpt, activeUserIds)
    } yield {
      items.groupBy(x => x._1).map { case (lpId, statuses) =>
        Map(
          "id" -> lpId,
          "total" -> statuses.flatMap { case (_, statuses) =>
            statuses
          }.groupBy { case (status, count) =>
            status
          }.map { case (status, statuses) =>
            (toReportCertificateStatus(status), statuses.map { case (_, count) => count }.sum)
          }
        )
      }
    }
  })

  get(s"$learningPatternReportPrefix/course/:courseId/certificate/:certificateId/goals/?")(await {
    val learningPathId = params.getAsOrElse[Long]("certificateId", halt(NotFound()))
    val userIds = multiParams.getAsOrElse[Long]("userIds", Nil)


    patternReportService.getUserGoalStatuses(learningPathId, userIds)

      .map { goals =>
        goals.filter(x => x.length > 0).map {
          x =>
            Map(
              "certificateId" -> learningPathId,
              "userId" -> x.head.userId,
              "goals" -> x.map { y =>
                Map(
                  "goalId" -> y.goalId,
                  "date" -> y.modifiedDate,
                  "status" -> toReportGoalStatus(y.status)
                )
              }
            )
        }
      }
  })

  get(s"$learningPatternReportPrefix/course/:courseId/certificate/:certificateId/total/?")(await {
    // courseId is not used, I don't know why it passed to backend
    val learningPathId = params.getAsOrElse[Long]("certificateId", halt(NotFound()))
    for {
      itemsF <- patternReportService.getTotalGoalStatus(learningPathId)
    } yield {
      itemsF.map { x =>
        val total = x._2.map {
          case (status, count) => (toReportGoalStatus(status), count)
        }.toMap

        Map(
          "id" -> x._1,
          "total" -> total
        )
      }
    }
  })

  private def toReportGoalStatus(status: GoalStatuses.Value) = {
    status match {
      case GoalStatuses.InProgress => PathsReportStatus.InProgress.id
      case GoalStatuses.Failed => PathsReportStatus.Failed.id
      case GoalStatuses.Success => PathsReportStatus.Achieved.id
      case GoalStatuses.Undefined => PathsReportStatus.Empty.id
      case _ => PathsReportStatus.Empty.id
    }
  }

  private def toReportCertificateStatus(status: CertificateStatuses.Value) = {
    status match {
      case CertificateStatuses.Success => PathsReportStatus.Achieved.id
      case CertificateStatuses.Failed => PathsReportStatus.Failed.id
      case CertificateStatuses.InProgress => PathsReportStatus.InProgress.id
      case CertificateStatuses.Overdue => PathsReportStatus.Expired.id
      case _ => PathsReportStatus.Empty.id
    }
  }

  private def getUserIds(courseId: Option[Long],
                         filter: Option[String],
                         skipTake: Option[SkipTake])(implicit companyId: Long) = {
    for {
      joinedUserIds <- patternReportService.getJoinedUserIds(courseId)
    } yield {
      skipTake match {
        case None => userService.search(filter, None) intersect(joinedUserIds)
        case Some(st) => userService.search(filter, None) intersect(joinedUserIds) drop(st.skip) take(st.take)
      }
    }
  }

  private def skipTake = {
    val skip = params.getAs[Int]("skip").getOrElse(0)
    val take = params.getAs[Int]("take").getOrElse(10)
    SkipTake(skip, take)
  }
}
