package com.arcusys.valamis.learningpath.impl

import java.util.Locale

import com.arcusys.valamis.learningpath.Configuration
import com.arcusys.valamis.learningpath.models.{WebContent, WebContentSort, WebContents}
import com.arcusys.valamis.learningpath.services.WebContentService
import com.liferay.portal.kernel.dao.orm.QueryUtil
import com.liferay.portal.kernel.language.LanguageUtil
import com.liferay.portal.kernel.workflow.WorkflowConstants
import com.liferay.portlet.journal.service.JournalArticleLocalServiceUtil

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

class WebContentServiceImpl(implicit executionContext: ExecutionContext)
  extends WebContentService {

  override def getAll(skip: Int, take: Int, sort: WebContentSort.Value, title:Option[String] = None)
                     (implicit companyId: Long): WebContents = {
    implicit val locale = Locale.getDefault
    val articles = JournalArticleLocalServiceUtil
      .getCompanyArticles(companyId, WorkflowConstants.STATUS_APPROVED, QueryUtil.ALL_POS, QueryUtil.ALL_POS)
      .asScala
      .groupBy(_.getArticleId)
      .values
      .map(_.maxBy(_.getVersion))
      .map(a => WebContent(a.getId, a.getTitle(locale)))
      .toSeq

    val filtered = title match {
      case Some(str) =>
        val filter = str.toLowerCase
        articles.filter(_.title.toLowerCase.contains(filter))
      case _ => articles
    }

    val sortedArticles = sort match {
      case WebContentSort.title => filtered.sortBy(_.title)
      case WebContentSort.titleDesc => filtered.sortBy(_.title).reverse
    }

    WebContents(sortedArticles.slice(skip, skip + take), filtered.length)
  }

  // TODO use user locate
  override def getWebContentTitle(id: Long): Future[Option[String]] = Future {
    val locale = Locale.getDefault
    Option(JournalArticleLocalServiceUtil.fetchJournalArticle(id)).map(_.getTitle(locale))
  }

  override def getContent(id: Long): Future[Option[String]] = Future {
    val locale = Locale.getDefault
    Option(JournalArticleLocalServiceUtil.fetchJournalArticle(id))
      .map { article =>
        val articleDisplay = JournalArticleLocalServiceUtil.getArticleDisplay(
          article.getGroupId,
          article.getArticleId,
          "view",
          LanguageUtil.getLanguageId(locale),
          null)
        JournalArticleLocalServiceUtil.getArticleContent(
          article,
          articleDisplay.getDDMTemplateKey,
          "view",
          LanguageUtil.getLanguageId(locale),
          null)
      }
  }

  override def getWebContentIdByClassPK(classPK: Long): Long = {
    JournalArticleLocalServiceUtil.getArticlesByResourcePrimKey(classPK)
      .asScala.headOption.map(_.getId).getOrElse(
      throw new NoSuchElementException("no Journal Article with ResourcePrimkey " + classPK))
  }

  override def addCheckerTask(webContentId: Long, userId: Long, companyId: Long): Unit = {
    Configuration.webContentListener.onWebContentViewed(userId, webContentId)(companyId)
  }
}
