package com.arcusys.valamis.learningpath.listeners

import com.arcusys.valamis.learningpath.Configuration
import com.arcusys.valamis.learningpath.utils.LiferayLogSupport
import com.arcusys.valamis.members.picker.model.MemberTypes
import com.liferay.portal.model._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}


class LPOrganizationListener extends BaseModelListener[Organization] with LiferayLogSupport {
  implicit val executionContext:ExecutionContext = Configuration.executionContext

  override def onAfterRemove(model: Organization): Unit = {
    await {
      Configuration.memberGroupListener
        .onRemoved(model.getOrganizationId, MemberTypes.Organization)(model.getCompanyId)
    }
  }

  //TODO: remove duplicate with other await
  private def await(f: Future[_]): Unit = {
    Await.ready(f, Duration.Inf).value.get
      .recover { case e: Throwable => log.error("Organization listener error", e) }
  }

}
