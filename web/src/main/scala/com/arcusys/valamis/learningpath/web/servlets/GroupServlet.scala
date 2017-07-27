package com.arcusys.valamis.learningpath.web.servlets

import com.arcusys.valamis.learningpath.models.LPWithSucceededUserCount
import com.arcusys.valamis.learningpath.services.{GroupService, LearningPathService}
import com.arcusys.valamis.learningpath.web.servlets.base.LearningPathServletBase
import com.arcusys.valamis.members.picker.model.{Member, MemberTypes, SkipTake}
import com.arcusys.valamis.members.picker.service.LiferayHelper
import org.scalatra.{BadRequest, NotFound}

import scala.concurrent.ExecutionContext

/**
  * Created by pkornilov on 6/1/17.
  */
trait GroupServlet {
  self: LearningPathServletBase =>

  implicit val executionContext: ExecutionContext

  protected val lfGroupsPrefix: String

  protected def learningPathService: LearningPathService

  protected def liferayHelper: LiferayHelper

  protected def groupService: GroupService

  //TODO add permission checking to GroupServlet
  get(lfGroupsPrefix + "/:id/learning-paths/?")(await {
    val group = Member(id = id, tpe = groupType,
      entityId = -1 //we don't use entity id in this servlet
    )
    if (!groupService.exists(group.id, group.tpe)) {
      halt(NotFound(s"There are no ${group.tpe} with id ${group.id}"))
    }
    val userIds = liferayHelper.getMemberUserIds(group)

    learningPathService.getByUserIdsWithSucceededUserCount(userIds, skipTake) map { results =>
      results map {
        case (lp, v, userCount) =>
          LPWithSucceededUserCount(
            id = lp.id,
            title = v.title,
            shortDescription = v.openBadgesDescription.getOrElse(""),
            description = v.description.getOrElse(""),
            logo = v.logo.map(l => s"learning-paths/logo-files/$l").getOrElse(""),
            succeededUserCount = userCount
          )
      }
    }
  })

  private def id = params.getAs[Long]("id") getOrElse {
    halt(BadRequest("bad group id"))
  }

  private def groupType: MemberTypes.Value = {
    params.getAs[String]("groupType") map { groupType =>
      MemberTypes.values filter {
        _ != MemberTypes.User
      } find {
        _.toString == groupType
      } getOrElse {
        halt(BadRequest(s"wrong groupType value: " + groupType))
      }
    } getOrElse {
      halt(BadRequest("missing groupType"))
    }
  }

  def skipTake: Option[SkipTake] = {
    val skip = params(request).getAs[Int]("skip")
    val take = params(request).getAs[Int]("take")
    (skip, take) match {
      case (Some(s), Some(t)) => Some(SkipTake(s, t))
      case (None, None) => None
      case _ => halt(BadRequest("skip and take should be used together"))
    }
  }

}
