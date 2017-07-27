package com.arcusys.valamis.learningpath.impl

import com.arcusys.valamis.learningpath.Configuration
import com.arcusys.valamis.learningpath.utils.LiferayLogSupport
import com.liferay.portlet.asset.model.AssetEntry
import com.liferay.portlet.asset.service.{AssetEntryLocalService, AssetEntryLocalServiceWrapper}

class LPAssetEntryLocalServiceWrapper(service: AssetEntryLocalService)
  extends AssetEntryLocalServiceWrapper(service)
    with LiferayLogSupport {


  override def incrementViewCounter(userId: Long, className: String, classPK: Long, increment: Int): AssetEntry = {

    val result = super.incrementViewCounter(userId, className, classPK, increment)
    try {
      if (className == "com.liferay.portlet.journal.model.JournalArticle") {
        Configuration.webContentListener.onWebContentViewedByClassPK(userId,
          classPK)(result.getCompanyId)
      }
    } catch {
      case e: Throwable => log.error("Failed to handle web content viewed event", e)
    }

    result
  }
}
