package com.arcusys.valamis.learningpath.asset

import com.arcusys.valamis.learningpath.impl.utils.LiferayLogSupport
import com.arcusys.valamis.learningpath.init.Configuration
import com.arcusys.valamis.learningpath.models.LearningPathWithVersion
import com.liferay.asset.kernel.model.{AssetRenderer, AssetRendererFactory, BaseAssetRendererFactory}
import com.liferay.asset.kernel.service.AssetEntryLocalServiceUtil
import org.osgi.service.component.annotations.Component

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by mminin on 31/03/2017.
  */

@Component(
  immediate = true,
  service = Array(classOf[AssetRendererFactory[_]]),
  property = Array(
    "javax.portlet.name=" + Configuration.LearningPathPortletId
  )
)
class LPAssetRendererFactory
  extends BaseAssetRendererFactory[LearningPathWithVersion]
    with LiferayLogSupport {

  setClassName(LPClassName)

  private implicit val executionContext = Configuration.executionContext

  private lazy val service = Configuration.learningPathService

  override def getType: String = "certificate"

  override def getAssetRenderer(classPK: Long, assetType: Int): AssetRenderer[LearningPathWithVersion] = {

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

