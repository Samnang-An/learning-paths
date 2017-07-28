package com.arcusys.valamis.learningpath.web.servlets

import com.arcusys.valamis.learningpath.models._
import com.arcusys.valamis.learningpath.services.{LearningPathService, UserProgressService}
import com.arcusys.valamis.learningpath.services.exceptions.NoLearningPathError
import com.arcusys.valamis.learningpath.web.servlets.base.{LearningPathServletBase, Permissions}
import com.arcusys.valamis.learningpath.web.servlets.response.LPForMemberResponse
import org.scalatra.{Forbidden, NotFound}

import scala.concurrent.ExecutionContext
import scala.util.Try

/**
  * Created by mminin on 31/03/2017.
  */
trait UserServlet {
  self: LearningPathServletBase =>

  implicit val executionContext: ExecutionContext

  protected val userPrefix: String
  protected val logoFilesPrefix: String

  protected def learningPathService: LearningPathService
  protected def userProgressService: UserProgressService

  private def userId: Long = if (params("id") == "current") {
    currentUserId
  } else {
    val id = Try(params("id").toLong).getOrElse(
      halt(NotFound(s"no user with id: ${params("id")}"))
    )

    if (id != currentUserId) requireModifyPermission
    id
  }

  private def lpId = params.getAsOrElse[Long]("lpId", {
    halt(NotFound(s"no learning path with id: ${params("lpId")}"))
  })

  implicit private val handleError: PartialFunction[Throwable, Nothing] = {
    case e: NoLearningPathError => halt(NotFound(s"no learning path with id: ${e.learningPathId}"))
  }

  private def filter = LearningPathFilter(
    params.getAs[String]("title"),
    params.getAs[Long]("courseId"),
    params.getAs[Long]("userId"),
    // without modify permission we returns only published and activated
    if (hasModifyPermission) params.getAs[Boolean]("published") else Some(true),
    if (hasModifyPermission) params.getAs[Boolean]("activated") else Some(true)
  )

  private def skip = params.getAs[Int]("skip")
  private def take = params.getAs[Int]("take").getOrElse(10)

  //TODO: add permission checker, need permission to get not current user id

  get(userPrefix + "/:id/learning-paths/:lpId/?")(await {
    val learningPathId = lpId
    val userMemberId = userId
    val allowUnpublished = hasModifyPermission

    learningPathService
      .getByIdForMember(learningPathId, userMemberId)
      .map {
        case None => throw new NoLearningPathError(learningPathId)
        case Some(data) if !data.learningPath.activated && !allowUnpublished => halt(Forbidden())
        case Some(data) => data
      }
      .map(toResponse)
  })

  get(userPrefix + "/:id/learning-paths/?") (await{
    val userMemberId = userId
    val joined = params.getAs[Boolean]("joined")

    val sort = LearningPathSort.withName(params.getOrElse("sort", "title"))
    val statusFilter = params.get("status").map(CertificateStatuses.withName)

    val itemsF = learningPathService
      .getByFilterForMember(filter, userMemberId, joined, statusFilter, sort, skip, Some(take))
    val countF = learningPathService
      .getCountByFilterForMember(filter, userMemberId, joined, statusFilter)

    for {
      items <- itemsF
      count <- countF
    } yield {
      Map("items" -> (items map toResponse), "total" -> count)
    }
  })


  private def toResponse(data: LPWithInfo) = {
    LPForMemberResponse.apply(data, logoFilesPrefix.replaceFirst("/", ""))
  }


  get(userPrefix + "/:id/learning-paths/total/?")(await {
    val userMemberId = userId
    val joined = params.getAs[Boolean]("joined")

    val statusFilter = params.get("status").map(CertificateStatuses.withName)

    learningPathService
      .getCountByFilterForMember(filter, userMemberId, joined, statusFilter)
      .map(count => Map("value" -> count))
  })

  get(userPrefix + "/:id/success-goals-count/?") (await{
    userProgressService.getCountByGoalsStatuses(userId, status = GoalStatuses.Success)
      .map(count => Map("value" -> count))
  })
}
