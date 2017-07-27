package com.arcusys.valamis.learningpath.web.servlets

import java.util.Date

import com.arcusys.valamis.learningpath.models.{CertificateTrackerFilter, WebContentSort}
import com.arcusys.valamis.learningpath.services._
import com.arcusys.valamis.learningpath.web.servlets.base.LearningPathServletBase
import com.arcusys.valamis.members.picker.model.SkipTake
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, ISODateTimeFormat}
import org.scalatra.NotFound

import scala.concurrent.ExecutionContext


trait CertificateTrackerServlet {
  self: LearningPathServletBase =>

  implicit val executionContext: ExecutionContext

  protected val expiredPrefix: String

  protected val webContentService: WebContentService

  protected def certificateTracker: CertificateTracker

  private val dateTimePattern = "yyyy-MM-dd"
  private val dateTimeFormat = DateTimeFormat.forPattern(dateTimePattern)


  private def startDate = params.get("startDate") map {
    DateTime.parse(_, dateTimeFormat)
  } getOrElse sevenDaysAgo

  private def endDate = (params.get("endDate") map {
    DateTime.parse(_, dateTimeFormat)
  } getOrElse today).plusDays(1)

  private def today = DateTime.now.withTimeAtStartOfDay

  private def sevenDaysAgo = today.minusDays(7)

  private def scopeId = params.getAs[Long]("scopeId")

  private def userIds = multiParams.getAs[Long]("userIds").getOrElse(Seq())

  private def notificationUserId = params.as[Long]("userId")

  private def expiredDate = {
    params.getAs[String]("expiredDate").map { date =>
      val parser = ISODateTimeFormat.dateTimeParser()
      parser.parseDateTime(date)
    }
  }


  private def skipTake = {
    val skip = params.getAs[Int]("skip").getOrElse(0)
    val take = params.getAs[Int]("take").getOrElse(10)
    Some(SkipTake(skip, take))
  }

  private def lpId = params.as[Long]("certificateId")

  private def certificateIds = multiParams.getAs[Long]("certificateIds").getOrElse(Seq())


  get(s"$expiredPrefix/?")(await {


    val filter = CertificateTrackerFilter(
      startDate,
      endDate,
      scopeId,
      userIds,
      skipTake,
      certificateIds
    )

    certificateTracker.getExpired(filter)

  })

  post(s"$expiredPrefix/send-notification/?") {
    expiredDate.foreach(date =>
      await {
        certificateTracker.sendNotification(
          lpId,
          notificationUserId,
          date
        )
      })
  }

  post(s"$expiredPrefix/send-notifications/?")(await {
    val filter = CertificateTrackerFilter(
      startDate,
      endDate,
      scopeId,
      userIds,
      skipTake,
      certificateIds
    )
    certificateTracker.sendNotifications(filter)
  })
}
