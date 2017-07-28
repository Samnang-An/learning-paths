package com.arcusys.valamis.learningpath.migration.impl

import com.arcusys.valamis.learningpath.services.AssetEntryService

/**
  * Created by pkornilov on 3/28/17.
  */
class AssetEntryServiceTestImpl(idMap: Map[Long, Long]) extends AssetEntryService {

  override def getAssetEntryUserId(className: String, classPK: Long): Option[Long] = {
    if (className == "com.arcusys.valamis.certificate.model.Certificate") {
      idMap.get(classPK)
    } else {
      throw new IllegalArgumentException("Wrong className: " + className)
    }
  }
}
