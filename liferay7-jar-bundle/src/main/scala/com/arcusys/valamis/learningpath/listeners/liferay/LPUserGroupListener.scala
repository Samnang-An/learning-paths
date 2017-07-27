package com.arcusys.valamis.learningpath.listeners.liferay

import com.arcusys.valamis.learningpath.impl.utils.LiferayLogSupport
import com.arcusys.valamis.learningpath.init.Configuration
import com.arcusys.valamis.members.picker.model.MemberTypes
import com.liferay.portal.kernel.model._
import org.osgi.service.component.annotations.Component

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}


@Component(
  name = "com.arcusys.valamis.learningpath.listeners.LPUserGroupListener",
  service = Array(classOf[ModelListener[_]])
)
class LPUserGroupListener extends BaseModelListener[UserGroup] with LiferayLogSupport {

  override def onAfterRemove(model: UserGroup): Unit = await {
    println(model.getUserGroupId)
    Configuration.memberGroupListener
      .onRemoved(model.getUserGroupId,
        MemberTypes.UserGroup)(model.getCompanyId)
  }


  private def await(f: Future[_]): Unit = {
    Await.ready(f, Duration.Inf).value.get
      .recover { case e: Throwable => log.error("UserGroup listener error", e) }
  }
}