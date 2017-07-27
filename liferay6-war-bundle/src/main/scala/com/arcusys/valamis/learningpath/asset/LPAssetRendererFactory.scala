package com.arcusys.valamis.learningpath.asset

import com.arcusys.valamis.learningpath.Configuration
import com.arcusys.valamis.learningpath.utils.LiferayLogSupport
import com.liferay.portlet.asset.model.{AssetRenderer, BaseAssetRendererFactory}
import com.liferay.portlet.asset.service.AssetEntryLocalServiceUtil

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by mminin on 01/04/2017.
  */

class LPAssetRendererFactory
  extends BaseAssetRendererFactory
    with LiferayLogSupport {

  private implicit val executionContext = Configuration.executionContext

  private lazy val service = Configuration.learningPathService

  override def getType: String = "certificate"

  override def getClassName: String = LPClassName

  override def getAssetRenderer(classPK: Long, assetType: Int): AssetRenderer = {

    val asset = AssetEntryLocalServiceUtil.getEntry(LPClassName, classPK)
    val companyId = asset.getCompanyId

    Await.result(service.getById(classPK)(companyId), Duration.Inf)
      .map { case (lp, version) =>
        new LPAssetRenderer(lp, version, asset)
      }
      .getOrElse {
        log.error("can't create AssetRenderer, no LP with id: " + classPK)
        null //TODO: test it
      }
  }
}

