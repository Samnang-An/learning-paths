package com.arcusys.valamis.learningpath.web.servlets

import com.arcusys.valamis.learningpath.models.{LearningPathFilter, LearningPathSort}
import com.arcusys.valamis.learningpath.services.exceptions.NoLearningPathError
import com.arcusys.valamis.learningpath.services.{GoalService, LearningPathService, UserProgressService}
import com.arcusys.valamis.learningpath.web.servlets.base.LearningPathServletBase
import com.arcusys.valamis.learningpath.web.servlets.response.RangeResponse
import com.arcusys.valamis.learningpath.web.servlets.response.statisticreport.{LPWithStatisticsResponse, UsersWithStatisticsResponse}
import com.arcusys.valamis.members.picker.model.search.{MemberFilter, MemberOrdering}
import com.arcusys.valamis.members.picker.model.{MemberTypes, SkipTake, UserInfo}
import com.arcusys.valamis.members.picker.service.MemberService
import org.scalatra.NotFound

import scala.concurrent.ExecutionContext


trait StatisticReportServlet {
  self: LearningPathServletBase =>

  implicit val executionContext: ExecutionContext

  protected val statisticReportPrefix: String
  protected val logoFilesPrefix: String

  protected def memberService: MemberService
  protected def goalService: GoalService
  protected def userProgressService: UserProgressService
  protected def learningPathService: LearningPathService

  implicit private val handleError: PartialFunction[Throwable, Nothing] = {
    case _: NoLearningPathError => halt(NotFound())
  }


  private def skip = params.getAs[Int]("skip").getOrElse(0)
  private def take = params.getAs[Int]("take").getOrElse(10)


  get(statisticReportPrefix + "/learning-paths/:id/users")(await {
    val lpId = params.as[Long]("id")
    val filter = MemberFilter(MemberTypes.User, searchText = None)
    val force = false
    val cId = companyId

    val ordering = MemberOrdering(!params.get("sort").contains("-name"))
    val skipTake = Some(SkipTake(skip, take))
    for {
      users <- memberService.getMembers(lpId, filter, ordering, skipTake, force)
      lpStatuses <- userProgressService.getUsersLPStatuses(lpId, users.records.map(_.id))(cId)
      goalStatusCounts <- userProgressService.getUsersGoalsCounts(lpId, users.records.map(_.id))(cId)
    } yield RangeResponse {
      users.map { user =>
        new UsersWithStatisticsResponse(
          user.asInstanceOf[UserInfo],
          lpStatuses.find(_.userId == user.id),
          goalStatusCounts.getOrElse(user.id, Map.empty)
        )
      }
    }
  })

  get(statisticReportPrefix + "/learning-paths/?")(await {
    val filter = LearningPathFilter(
      params.getAs[String]("title"),
      params.getAs[Long]("courseId"),
      params.getAs[Long]("userId"),
      // returns only published and activated
      Some(true),
      Some(true)
    )

    val cId = companyId
    val sort = LearningPathSort.withName(params.getOrElse("sort", "title"))

    val itemsF = learningPathService.getByFilter(filter, sort, Some(skip), take)
    val countF = learningPathService.getCountByFilter(filter)

    for {
      items <- itemsF
      count <- countF
      lpStatusCounts <- userProgressService.getCountsByLPStatuses(
        items.map { case (lp, _) => lp.id }
      )(cId)
    } yield {
      RangeResponse(
        items.map { case (lp, version) =>
          val counts = lpStatusCounts.getOrElse(lp.id, Map.empty)
          LPWithStatisticsResponse.apply(lp, version, counts, logoFilesPrefix.replaceFirst("/", ""))
        },
        count
      )
    }
  })

}
