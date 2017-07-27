package com.arcusys.valamis.learningpath.web.servlets

import com.arcusys.valamis.learningpath.services.exceptions.NoLearningPathError
import com.arcusys.valamis.learningpath.services.{CertificateNotificationService, GoalService, LearningPathService, UserProgressService}
import com.arcusys.valamis.learningpath.web.servlets.base.LearningPathServletBase
import com.arcusys.valamis.learningpath.web.servlets.response.statisticreport.UsersWithStatisticsResponse
import com.arcusys.valamis.learningpath.web.servlets.response.{RangeResponse, UserMemberResponse}
import com.arcusys.valamis.members.picker.model.search.{MemberFilter, MemberOrdering}
import com.arcusys.valamis.members.picker.model.{Member, MemberTypes, SkipTake, UserInfo}
import com.arcusys.valamis.members.picker.service.MemberService
import org.scalatra.NotFound

import scala.concurrent.ExecutionContext


trait MembersServlet {
  self: LearningPathServletBase =>

  implicit val executionContext: ExecutionContext

  protected val learningPathsPrefix: String
  protected val goalsPrefix: String
  private val force = true

  protected def memberService: MemberService
  protected def goalService: GoalService
  protected def userProgressService: UserProgressService
  protected def certificateNotificationService: CertificateNotificationService
  protected def learningPathService: LearningPathService

  implicit private val handleError: PartialFunction[Throwable, Nothing] = {
    case _: NoLearningPathError => halt(NotFound())
  }

  private def id = params.as[Long]("id")

  private def ordering = {
    //default 'name' other case '-name'
    MemberOrdering(!params.get("sort").contains("-name"))
  }

  private def skipTake = {
    val skip = params.getAs[Int]("skip").getOrElse(0)
    val take = params.getAs[Int]("take").getOrElse(10)
    Some(SkipTake(skip, take))
  }

  private def memberFilter = MemberFilter(memberType, searchText = params.get("name"))

  private def memberType = {
    membersFolderToType.getOrElse(params("type"), halt(NotFound()))
  }

  private def membersFolderToType = Map(
    "users" -> MemberTypes.User,
    "groups" -> MemberTypes.UserGroup,
    "roles" -> MemberTypes.Role,
    "organizations" -> MemberTypes.Organization
  )

  get(s"$learningPathsPrefix/:id/members/:type/?")(await {
    memberService.getMembers(id, memberFilter, ordering, skipTake, force)
      .map(RangeResponse(_))
  })

  get(s"$learningPathsPrefix/:id/members/users/?")(await {
    val lpId = id
    val filter = MemberFilter(MemberTypes.User, searchText = params.get("name"))
    //original companyId not available from Future
    //todo: remove for
    val cId = companyId

    for {
      users <- memberService.getMembers(lpId, filter, ordering, skipTake, force)
      statuses <- userProgressService.getUsersLPStatuses(lpId, users.records.map(_.id))(cId)
    } yield RangeResponse {
      users.map { user =>
        new UserMemberResponse(user.asInstanceOf[UserInfo], statuses.find(_.userId == user.id))
      }
    }
  })

  // api for valamis 'my certificates report'
  // TODO: find better way to integrate to our api
  get(s"$learningPathsPrefix/:id/members/users/with-goal-counts")(await {
    val lpId = id
    val filter = MemberFilter(MemberTypes.User, searchText = params.get("name"))
    //original companyId not available from Future
    val cId = companyId

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

  get(s"$learningPathsPrefix/:id/available-members/:type/?")(await {
    memberService.getAvailableMembers(id, memberFilter, ordering, skipTake, force)
      .map(RangeResponse(_))
  })


  post(s"$learningPathsPrefix/:id/members/:type/?")(await {
    requireModifyPermission
    val learningPathId = id
    memberService.addMembers {
      parsedBody.extract[Seq[Long]].map { memberId =>
         Member(memberId, memberType, learningPathId)
      }
    }
  })

  delete(s"$learningPathsPrefix/:id/members/:type/:memberId/?")(await {
    requireModifyPermission

    val memberId = params.as[Long]("memberId")
    val learningPathId = id

    memberType match {
      case MemberTypes.User =>
        memberService.deleteUser(learningPathId, memberId)
      case _ =>
        memberService.deleteMember(Member(memberId, memberType, learningPathId))
    }
  })

  get(s"$learningPathsPrefix/:id/members/users/:userId/goals-progress/?")(await {
    val userId = params.as[Long]("userId")

    userProgressService.getUserGoalsStatuses(id, userId)
  })

  get(s"$learningPathsPrefix/:id/members/users/:userId/progress/?")(await {
    val userId = params.as[Long]("userId")

    userProgressService.getUserLPStatuses(id, userId).map(
      _ getOrElse halt(NotFound())
    )
  })

  post(s"$learningPathsPrefix/:id/join/?")(await {

    val learningPathId = id
    val userMemberId = currentUserId

    memberService.addMembers(Seq(Member(userMemberId, MemberTypes.User, learningPathId)))
  })

  post(s"$learningPathsPrefix/:id/leave/?")(await {

    val learningPathId = id
    val userMemberId = currentUserId

    memberService.deleteUser(learningPathId, userMemberId)
  })

}
