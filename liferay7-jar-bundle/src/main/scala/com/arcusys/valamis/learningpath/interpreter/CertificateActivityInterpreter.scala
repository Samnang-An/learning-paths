package com.arcusys.valamis.learningpath.interpreter

import com.arcusys.valamis.learningpath.impl.utils.LiferayLogSupport
import com.arcusys.valamis.learningpath.init.Configuration
import com.arcusys.valamis.learningpath.models.CertificateActivitesType
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal
import com.liferay.portal.kernel.service.ServiceContext
import com.liferay.portal.kernel.util.{ResourceBundleLoader, StringPool}
import com.liferay.social.kernel.model.{BaseSocialActivityInterpreter, SocialActivity, SocialActivityFeedEntry}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.language.postfixOps

object CertificateActivityInterpreter {
  val Certificate = "com.arcusys.valamis.certificate.model.Certificate"
  val CertificateActivityType = "com.arcusys.valamis.certificate.model.CertificateActivityType"
  val CertificateStateType = "com.arcusys.valamis.certificate.model.CertificateStateType"
  val LearningPath = "com.arcusys.valamis.learningpath.models.LearningPath"
}

class CertificateActivityInterpreter extends BaseSocialActivityInterpreter
    with LiferayLogSupport {

  implicit private val executionContext = Configuration.executionContext

  private lazy val learningPathService = Configuration.learningPathService

  override protected def doInterpret(activity: SocialActivity, context: ServiceContext): SocialActivityFeedEntry = {
    val companyId = CompanyThreadLocal.getCompanyId

    val activityFeedEntry = if (activity.getCompanyId != companyId) {
      log.debug(s"companyId mismatch (activity companyId: ${activity.getCompanyId}, current companyId: $companyId)")
      None
    } else {
      val creatorUserName = getUserName(activity.getUserId, context)
      val activityType: Int = activity.getType

      val title = activity.getClassName match {
        case CertificateActivityInterpreter.Certificate =>
          if (activityType == 2) "achieved a certificate" // 2 - was in valamis
        case CertificateActivityInterpreter.CertificateActivityType => "joined a certificate"
        case CertificateActivityInterpreter.CertificateStateType =>
          if (activityType == CertificateActivitesType.Published.id) "published a certificate"
        case CertificateActivityInterpreter.LearningPath =>

        activityType match {
            case a if a == CertificateActivitesType.Published.id => "published a certificate"
            case a if a == CertificateActivitesType.Expired.id => "expired a certificate"
            case a if a == CertificateActivitesType.Failed.id => "failed a certificate"
            case a if a == CertificateActivitesType.Achieved.id => "achieved a certificate"
            case a if a == CertificateActivitesType.UserJoined.id => "joined a certificate"
            case _ =>
          }
      }

      val certificate = learningPathService.getById(activity.getClassPK.toInt)(companyId)
      val feedEntryF = certificate.map(_.map { case (lp, version) =>
        val sb = new StringBuilder
        sb.append(creatorUserName + " ")
        sb.append(title + " ")
        sb.append(version.title)

        new SocialActivityFeedEntry(StringPool.BLANK, sb.toString(), StringPool.BLANK)
      })

      Await.result(feedEntryF, Duration.Inf)
    }

    activityFeedEntry orNull
  }

  def getClassNames() = Array(CertificateActivityInterpreter.Certificate,
    CertificateActivityInterpreter.CertificateActivityType,
    CertificateActivityInterpreter.CertificateStateType,
    CertificateActivityInterpreter.LearningPath)

  override def getResourceBundleLoader: ResourceBundleLoader = ???
}
