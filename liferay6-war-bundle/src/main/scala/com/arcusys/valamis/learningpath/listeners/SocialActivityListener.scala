package com.arcusys.valamis.learningpath.listeners

import com.arcusys.valamis.learningpath.Configuration
import com.arcusys.valamis.learningpath.utils.LiferayLogSupport
import com.liferay.portal.model.BaseModelListener
import com.liferay.portal.security.auth.CompanyThreadLocal
import com.liferay.portlet.social.model.SocialActivity

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Try

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
