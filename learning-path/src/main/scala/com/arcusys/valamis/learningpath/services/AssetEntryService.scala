package com.arcusys.valamis.learningpath.services


/**
  * Created by pkornilov on 3/24/17.
  */
trait AssetEntryService {

  def getAssetEntryUserId(className: String, classPK: Long): Option[Long]

}
