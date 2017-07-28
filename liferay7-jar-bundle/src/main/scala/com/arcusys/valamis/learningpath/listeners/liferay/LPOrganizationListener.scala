package com.arcusys.valamis.learningpath.listeners.liferay

import com.arcusys.valamis.learningpath.impl.utils.LiferayLogSupport
import com.arcusys.valamis.learningpath.init.Configuration
import com.arcusys.valamis.members.picker.model.MemberTypes
import com.liferay.portal.kernel.model._
import org.osgi.service.component.annotations.Component

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}


@Component(
  name = "com.arcusys.valamis.learningpath.listeners.LPOrganizationListener",
  service = Array(classOf[ModelListener[_]])
)
class LPOrganizationListener extends BaseModelListener[Organization] with LiferayLogSupport {

  override def onAfterRemove(model: Organization): Unit = await {
    Configuration.memberGroupListener
      .onRemoved(model.getOrganizationId,
        MemberTypes.Organization)(model.getCompanyId)
  }


  private def await(f: Future[_]): Unit = {
    Await.ready(f, Duration.Inf).value.get
      .recover { case e: Throwable => log.error("Organization listener error", e) }
  }
}
