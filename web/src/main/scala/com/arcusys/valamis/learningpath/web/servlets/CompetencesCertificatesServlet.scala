package com.arcusys.valamis.learningpath.web.servlets

import com.arcusys.valamis.learningpath.models.LPWithSucceededUsers
import com.arcusys.valamis.learningpath.services.LearningPathService
import com.arcusys.valamis.learningpath.web.servlets.base.LearningPathServletBase
import com.arcusys.valamis.members.picker.model.IdAndName
import com.arcusys.valamis.members.picker.service.LiferayHelper
import org.scalatra.BadRequest

import scala.concurrent.ExecutionContext

/**
  * Created by pkornilov on 5/29/17.
  */
trait CompetencesCertificatesServlet {
  self: LearningPathServletBase =>

  protected val competencesCertificatesPrefix: String

  protected def learningPathService: LearningPathService

  protected def liferayHelper: LiferayHelper

  implicit val executionContext: ExecutionContext

  //TODO add permission checking
  get(competencesCertificatesPrefix + "/?") (await {
    learningPathService.getByTitleWithSucceededUserIds(title, count) map { results =>
      results map {
        case (lp, v, userIds) =>

          val users = userIds map { id =>
            val user = liferayHelper.getUserInfo(id, Seq())
            IdAndName(id = user.id,
              name = user.name)
          }

          LPWithSucceededUsers(
            id = lp.id,
            title = v.title,
            shortDescription = v.openBadgesDescription.getOrElse(""),
            description = v.description.getOrElse(""),
            logo = v.logo.map(l => s"learning-paths/logo-files/$l").getOrElse(""),
            succeededUsers = users
          )
      }
    }
  })

  private def title: String = params.getAs[String]("title") getOrElse {
    halt(BadRequest("'title' is missing"))
  }

  private def count: Option[Int] = params.getAs[Int]("count")

}
