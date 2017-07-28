package com.arcusys.valamis.learningpath.listeners.liferay

import com.arcusys.valamis.learningpath.impl.utils.LiferayLogSupport
import com.arcusys.valamis.learningpath.init.Configuration
import com.arcusys.valamis.members.picker.model.MemberTypes
import com.liferay.portal.kernel.model._
import org.osgi.service.component.annotations.Component

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}


@Component(
  name = "com.arcusys.valamis.learningpath.listeners.LPRoleListener",
  service = Array(classOf[ModelListener[_]])
)
class LPRoleListener extends BaseModelListener[Role] with LiferayLogSupport {

  override def onAfterRemove(model: Role): Unit = await {
    Configuration.memberGroupListener
      .onRemoved(model.getRoleId,
        MemberTypes.Role)(model.getCompanyId)
  }


  private def await(f: Future[_]): Unit = {
    Await.ready(f, Duration.Inf).value.get
      .recover { case e: Throwable => log.error("Role listener error", e) }
  }
}
