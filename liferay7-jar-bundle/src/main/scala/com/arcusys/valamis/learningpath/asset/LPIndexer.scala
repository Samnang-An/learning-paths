package com.arcusys.valamis.learningpath.asset

import java.util.Locale
import javax.portlet.{PortletRequest, PortletResponse}

import com.arcusys.valamis.learningpath.init.Configuration
import com.arcusys.valamis.learningpath.models.{LearningPathFilter, LearningPathSort, LearningPathWithVersion}
import com.arcusys.valamis.learningpath.services.LearningPathService
import com.liferay.portal.kernel.search._
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal
import com.liferay.portal.kernel.util.{GetterUtil, StringUtil, Validator}
import org.osgi.service.component.annotations.Component

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by mminin on 31/03/2017.
  */

@Component(
  name = "com.arcusys.valamis.learningpath.asset.LearningPathIndexer",
  service = Array(classOf[Indexer[_]]),
  property = Array(
    "javax.portlet.name=" + Configuration.LearningPathPortletId
  )
)
class LPIndexer extends BaseIndexer[LearningPathWithVersion] {

  private implicit val executionContext = Configuration.executionContext

  private lazy val service: LearningPathService = Configuration.learningPathService

  private val commitImmediately = false

  override def getClassName: String = LPClassName

  override def doDelete(data: LearningPathWithVersion): Unit = {
    deleteDocument(data.learningPath.companyId, data.learningPath.id)
  }

  override def doGetDocument(data: LearningPathWithVersion): Document = {
    val document = new DocumentImpl

    document.addUID(Configuration.LearningPathPortletId, data.learningPath.id)
    document.addDate(Field.MODIFIED_DATE, data.version.modifiedDate.toDate)

    for (description <- data.version.description)
      document.addText(Field.DESCRIPTION, description)

    document.addText(Field.TITLE, data.version.title)
    document.addKeyword(Field.COMPANY_ID, data.learningPath.companyId)
    document.addKeyword(Field.ENTRY_CLASS_NAME, getClassName)
    document.addKeyword(Field.ENTRY_CLASS_PK, data.learningPath.id)

    for (courseId <- data.version.courseId) {
      document.addKeyword(Field.GROUP_ID, getSiteGroupId(courseId))
      document.addKeyword(Field.SCOPE_GROUP_ID, courseId)
    }

    document
  }

  override def doReindex(className: String, classPK: Long): Unit = {
    val companyId = CompanyThreadLocal.getCompanyId

    if (className == getClassName) {
      Await.result(service.getById(classPK)(companyId), Duration.Inf)
        .foreach { data =>
          doReindex(LearningPathWithVersion.tupled(data))
        }
    }
  }

  override def doReindex(data: LearningPathWithVersion): Unit = {
    val document = getDocument(data)

    IndexWriterHelperUtil
      .updateDocument(getSearchEngineId, data.learningPath.companyId, document, commitImmediately)
  }

  override def doReindex(ids: Array[String]): Unit = {
    //ids contains companyId by tutorial
    implicit val companyId = GetterUtil.getLong(ids(0))

    val filter = LearningPathFilter(None, None, None, None, None)
    //TODO: add get all method and remove 1000
    val documents = Await.result(
      service.getByFilter(filter, LearningPathSort.title, None, 1000),
      Duration.Inf
    ) map { data =>
      getDocument(LearningPathWithVersion.tupled(data))
    }

    IndexWriterHelperUtil
      .updateDocuments(getSearchEngineId, companyId, documents.asJava, commitImmediately)
  }

  override def doGetSummary(document: Document,
                            locale: Locale, snippet: String,
                            portletRequest: PortletRequest,
                            portletResponse: PortletResponse): Summary = {

    val title = document.get(Field.TITLE)
    val content = {
      if (Validator.isNull(snippet) && Validator.isNull(document.get(Field.DESCRIPTION))) {
        val length = 200 //value from documentation
        StringUtil.shorten(document.get(Field.CONTENT), length)
      }
      else if (Validator.isNull(snippet)) {
        document.get(Field.DESCRIPTION)
      }
      else {
        snippet
      }
    }

    new Summary(title, content)
  }

}