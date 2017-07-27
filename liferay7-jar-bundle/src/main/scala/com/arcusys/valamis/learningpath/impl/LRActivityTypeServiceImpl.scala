package com.arcusys.valamis.learningpath.impl

import java.util.Locale

import com.arcusys.valamis.learningpath.models.LRActivityType
import com.arcusys.valamis.learningpath.services.LRActivityTypeService
import com.liferay.blogs.kernel.model.BlogsEntry
import com.liferay.bookmarks.model.BookmarksEntry
import com.liferay.calendar.model.CalendarBooking
import com.liferay.document.library.kernel.model.DLFileEntry
import com.liferay.message.boards.kernel.model.MBMessage
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil
import com.liferay.portal.kernel.language.LanguageUtil
import com.liferay.portal.kernel.util.PortalUtil
import com.liferay.social.kernel.service.{SocialActivityCounterLocalServiceUtil, SocialActivityLocalServiceUtil}
import com.liferay.wiki.model.WikiPage
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by mminin on 17/02/2017.
  */
class LRActivityTypeServiceImpl(implicit executionContext: ExecutionContext)
  extends LRActivityTypeService {

  override def getAll(implicit companyId: Long): Future[Seq[LRActivityType]] = {
    implicit val locale = Locale.getDefault

    Future {
      socialActivities ++ counts
    }
  }

  private def socialActivities(implicit locale: Locale) = Seq(
    LRActivityType(classOf[BlogsEntry].getCanonicalName, translate("blogs")),
    LRActivityType(classOf[DLFileEntry].getCanonicalName, translate("file-upload")),
    LRActivityType(classOf[WikiPage].getCanonicalName, translate("wiki")),
    LRActivityType(classOf[MBMessage].getCanonicalName, translate("message-board-messages")),
    LRActivityType(classOf[CalendarBooking].getCanonicalName, translate("calendar-event")),
    LRActivityType(classOf[BookmarksEntry].getCanonicalName, translate("social-bookmarks"))
  )

  private def counts(implicit locale: Locale) = Seq(
    LRActivityType("participation", translate("participation-value")),
    LRActivityType("contribution", translate("group.statistics.title.contribution"))
  )

  private def translate(key: String)
                       (implicit locale: Locale): String = {
    LanguageUtil.get(Locale.getDefault, key)
  }

  override def getLRActivityCountByUser(userId: Long,
                                        activityId: String,
                                        dateFrom: DateTime)
                                       (implicit companyId: Long): Future[Long] = {
    Future {
      activityId match {
        case "participation" | "contribution" =>
          SocialActivityCounterLocalServiceUtil.dynamicQueryCount {
            SocialActivityCounterLocalServiceUtil.dynamicQuery
              .add(RestrictionsFactoryUtil.eq("classPK", userId)) // user Id in classpk column
              .add(RestrictionsFactoryUtil.eq("name", activityId))
          }
        case _ =>
          val classNameId = PortalUtil.getClassNameId(activityId)
          SocialActivityLocalServiceUtil.dynamicQueryCount {
            SocialActivityLocalServiceUtil.dynamicQuery
              .add(RestrictionsFactoryUtil.eq("userId", userId))
              .add(RestrictionsFactoryUtil.eq("classNameId", classNameId))
              .add(RestrictionsFactoryUtil.gt("createDate", dateFrom.toDate.getTime))
          }
      }

    }
  }
}
