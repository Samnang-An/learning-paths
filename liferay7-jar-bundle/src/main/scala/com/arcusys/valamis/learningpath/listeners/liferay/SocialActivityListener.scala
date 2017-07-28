package com.arcusys.valamis.learningpath.listeners.liferay

import com.arcusys.valamis.learningpath.impl.utils.LiferayLogSupport
import com.arcusys.valamis.learningpath.init.Configuration
import com.liferay.portal.kernel.model.{BaseModelListener, ModelListener}
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal
import com.liferay.social.kernel.model.SocialActivity
import org.osgi.service.component.annotations.Component

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Try

@Component(
  name = "com.arcusys.valamis.learningpath.listeners.SocialActivityListener",
  service = Array(classOf[ModelListener[_]])
)
class SocialActivityListener extends BaseModelListener[SocialActivity]
  with LiferayLogSupport {

  //TODO: add certificate activity types to ignore when will be ready
  private val unsupportedForChecking = Set[String](
    //    CertificateStateType.getClass.getName,
    //    CertificateActivityType.getClass.getName
  )

  // this method should not be aborted by exception, it will broken liferay socialActivity entity
  override def onAfterCreate(socialActivity: SocialActivity): Unit = Try {

    val userId = socialActivity.getUserId
    val activityId = socialActivity.getClassName
    implicit val companyId = socialActivity.getCompanyId

    // we need to setup company id for ModelListener
    CompanyThreadLocal.setCompanyId(companyId)


    if (!unsupportedForChecking.contains(activityId)) {
      Await.result(
        Configuration.lrActivityListener.onLRActivityCreated(userId, activityId),
        Duration.Inf
      )
    }

  } recover {
    case e: Throwable => log.error(e.getMessage, e)
  }
}
