package com.arcusys.valamis.learningpath.impl

import com.arcusys.valamis.learningpath.services.AssetEntryService
import com.liferay.portlet.asset.service.AssetEntryLocalServiceUtil

/**
  * Created by pkornilov on 3/24/17.
  */
class AssetEntryServiceImpl extends AssetEntryService {

  override def getAssetEntryUserId(className: String, classPK: Long): Option[Long] = {
    Option(AssetEntryLocalServiceUtil.fetchEntry(className, classPK)) flatMap { entry =>
      Some(entry.getUserId).filter(_ > 0)
    }
  }

}
