package com.arcusys.valamis.learningpath.impl

import com.arcusys.valamis.learningpath.impl.utils.LiferayLogSupport
import com.arcusys.valamis.learningpath.init.Configuration
import com.liferay.asset.kernel.service.AssetEntryLocalServiceWrapper
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal
import com.liferay.portal.kernel.service.ServiceWrapper
import org.osgi.service.component.annotations.Component


@Component(
  property = Array[String](),
  immediate = true,
  service = Array(classOf[ServiceWrapper[_]])
)
class LPAssetEntryLocalServiceWrapper
  extends AssetEntryLocalServiceWrapper(null)
    with LiferayLogSupport {

  @Override
  override def incrementViewCounter(userId: Long, className: String, classPK: Long, increment: Int): Unit = {

    super.incrementViewCounter(userId, className, classPK, increment)
    try {
      if (className == "com.liferay.journal.model.JournalArticle") {
        Configuration.webContentListener.onWebContentViewedByClassPK(userId,
          classPK)(CompanyThreadLocal.getCompanyId)
      }
    } catch {
      case e: Throwable => log.error("Failed to handle web content viewed event", e)
    }
  }
}
