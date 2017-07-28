package com.arcusys.valamis.learningpath.asset

import java.util.Locale
import javax.portlet.PortletURL

import com.arcusys.valamis.learningpath.Configuration
import com.arcusys.valamis.learningpath.models.{LearningPathFilter, LearningPathSort, LearningPathWithVersion}
import com.arcusys.valamis.learningpath.services.LearningPathService
import com.liferay.portal.kernel.search._
import com.liferay.portal.kernel.util.{GetterUtil, StringUtil, Validator}
import com.liferay.portal.security.auth.CompanyThreadLocal

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by mminin on 01/04/2017.
  */
class LPIndexer extends BaseIndexer {
  private type Model = LearningPathWithVersion


  private implicit val executionContext = Configuration.executionContext

  private lazy val service: LearningPathService = Configuration.learningPathService

  private val commitImmediately = false

  override def getClassNames: Array[String] = Array(LPClassName)

  override def getPortletId: String = Configuration.LearningPathPortletId

  override def getPortletId(searchContext: SearchContext): String = {
    Configuration.LearningPathPortletId
  }


  override def doDelete(obj: scala.Any): Unit = {
    val data = obj.asInstanceOf[Model]
    deleteDocument(data.learningPath.companyId, data.learningPath.id)
  }

  override def doGetDocument(obj: scala.Any): Document = {
    val data = obj.asInstanceOf[Model]

    val document = new DocumentImpl

    document.addUID(Configuration.LearningPathPortletId, data.learningPath.id)

    document.addKeyword(Field.COMPANY_ID, data.learningPath.companyId)
    document.addKeyword(Field.ENTRY_CLASS_NAME, LPClassName)
    document.addKeyword(Field.ENTRY_CLASS_PK, data.learningPath.id)

    document.addText(Field.TITLE, data.version.title)
    document.addDate(Field.MODIFIED_DATE, data.version.modifiedDate.toDate)

    for (description <- data.version.description)
      document.addText(Field.DESCRIPTION, description)

    for (courseId <- data.version.courseId) {
      document.addKeyword(Field.GROUP_ID, getSiteGroupId(courseId))
      document.addKeyword(Field.SCOPE_GROUP_ID, courseId)
    }

    document
  }

  override def doReindex(className: String, classPK: Long): Unit = {
    val companyId = CompanyThreadLocal.getCompanyId

    if (className == LPClassName) {
      Await.result(service.getById(classPK)(companyId), Duration.Inf)
        .foreach { data =>
          doReindex(LearningPathWithVersion.tupled(data))
        }
    }
  }

  override def doReindex(obj: scala.Any): Unit = {
    val data = obj.asInstanceOf[Model]

    val document = getDocument(data)

    SearchEngineUtil
      .updateDocument(getSearchEngineId, data.learningPath.companyId, document, commitImmediately)
  }

  override def doReindex(ids: Array[String]): Unit = {
    //ids contains companyId by tutorial
    implicit val companyId = GetterUtil.getLong(ids(0))

    val filter = LearningPathFilter(None, None, None, published = Some(true), activated = Some(true))
    //TODO: add get all method and remove 1000
    val documents = Await.result(
      service.getByFilter(filter, LearningPathSort.title, None, 1000),
      Duration.Inf
    ) map { data =>
      getDocument(LearningPathWithVersion.tupled(data))
    }

    SearchEngineUtil
      .updateDocuments(getSearchEngineId, companyId, documents.asJava, commitImmediately)
  }

  override def doGetSummary(document: Document,
                            locale: Locale, snippet: String,
                            portletURL: PortletURL): Summary = {

    val title = document.get(Field.TITLE)
    val content =
      if (!Validator.isNull(snippet)) {
        snippet
      } else {
        if (!Validator.isNull(document.get(Field.DESCRIPTION))) {
          document.get(Field.DESCRIPTION)
        } else {
          val length = 200 //value from documentation
          StringUtil.shorten(document.get(Field.CONTENT), length)
        }
      }

    val resourcePrimKey = document.get(Field.ENTRY_CLASS_PK)
    portletURL.setParameter("resourcePrimKey", resourcePrimKey)
    new Summary(title, content, portletURL)
  }
}