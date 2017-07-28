package com.arcusys.valamis.learningpath.asset

import com.arcusys.valamis.learningpath.impl.utils.LiferayLogSupport
import com.arcusys.valamis.learningpath.init.Configuration
import com.arcusys.valamis.learningpath.models.{LPVersion, LearningPath, LearningPathWithVersion}
import com.arcusys.valamis.learningpath.services.AssetService
import com.liferay.asset.kernel.model.AssetEntry
import com.liferay.asset.kernel.service.AssetEntryLocalServiceUtil
import com.liferay.counter.kernel.service.CounterLocalServiceUtil
import com.liferay.portal.kernel.search.IndexerRegistryUtil
import com.liferay.portal.kernel.service.{ClassNameLocalServiceUtil, CompanyLocalServiceUtil, UserLocalServiceUtil}
import com.liferay.portal.kernel.util.ContentTypes

import scala.concurrent.Future

/**
  * Created by mminin on 31/03/2017.
  */
class AssetServiceImpl
  extends AssetService
    with LiferayLogSupport {

  private implicit val executionContext = Configuration.executionContext

  override def create(learningPath: LearningPath,
                      version: LPVersion): Future[Unit] = Future {
    val asset = AssetEntryLocalServiceUtil.createAssetEntry(CounterLocalServiceUtil.increment)

    fillAssetEntry(learningPath, version, asset)

    AssetEntryLocalServiceUtil.updateAssetEntry(asset)

    if (asset.isVisible) updateIndex(learningPath, version)
  }

  override def update(learningPath: LearningPath,
                      version: LPVersion): Future[Unit] = Future {
    val asset = Option(AssetEntryLocalServiceUtil.fetchEntry(LPClassName, learningPath.id))
      .getOrElse {
        log.error("no LP asset with classPK: " + learningPath.id)
        AssetEntryLocalServiceUtil.createAssetEntry(CounterLocalServiceUtil.increment)
      }

    //TODO: do not set static fields second time (user, company, classname, ...)
    fillAssetEntry(learningPath, version, asset)

    AssetEntryLocalServiceUtil.updateAssetEntry(asset)

    if (asset.isVisible) updateIndex(learningPath, version)
    else deleteIndex(learningPath, version)
  }

  override def delete(learningPath: LearningPath,
                      version: LPVersion): Future[Unit] = Future {

    deleteIndex(learningPath, version)

    AssetEntryLocalServiceUtil.deleteEntry(LPClassName, learningPath.id)
  }

  private def fillAssetEntry(learningPath: LearningPath,
                             version: LPVersion,
                             asset: AssetEntry): Unit = {

    asset.setClassPK(learningPath.id)
    asset.setClassName(LPClassName)
    asset.setClassNameId(ClassNameLocalServiceUtil.getClassNameId(LPClassName))
    asset.setTitle(version.title)

    asset.setSummary(version.description getOrElse version.title)
    asset.setCompanyId(learningPath.companyId)

    asset.setGroupId {
      version.courseId getOrElse {
        CompanyLocalServiceUtil.getCompany(learningPath.companyId).getGroupId
      }
    }

    asset.setUserId(learningPath.userId)
    asset.setUserName{
      Option(UserLocalServiceUtil.fetchUser(learningPath.userId))
        .map(_.getFullName)
        .getOrElse("")
    }

    asset.setMimeType(ContentTypes.TEXT_HTML)
    asset.setVisible(learningPath.activated)
  }

  private def updateIndex(learningPath: LearningPath,
                          version: LPVersion): Unit = {
    Option(IndexerRegistryUtil.getIndexer[LearningPathWithVersion](LPClassName)) match {
      case None => log.error("LP Indexer not found")
      case Some(indexer) => indexer.reindex(LearningPathWithVersion(learningPath, version))
    }
  }

  private def deleteIndex(learningPath: LearningPath,
                          version: LPVersion): Unit = {
    Option(IndexerRegistryUtil.getIndexer[LearningPathWithVersion](LPClassName)) match {
      case None => log.error("LP Indexer not found")
      case Some(indexer) => indexer.delete(LearningPathWithVersion(learningPath, version))
    }
  }
}
