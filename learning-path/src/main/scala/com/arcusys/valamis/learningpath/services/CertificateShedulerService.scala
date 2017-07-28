package com.arcusys.valamis.learningpath.services

import com.arcusys.valamis.learningpath.models.{CertificateActivitesType, LearningPath}
import com.arcusys.valamis.learningpath.utils.{DbActions, DbActionsSupport}
import org.apache.commons.logging.Log
import org.joda.time.{DateTime, Days}

import scala.concurrent.ExecutionContext


class CertificateShedulerService(val dbActions: DbActions,
                                 certificateTracker: CertificateTracker,
                                 socialActivityHelper: SocialActivityHelper,
                                 log: Log)
                                (implicit val executionContext: ExecutionContext)
extends DbActionsSupport {


 def doAction(): Unit = {
  val expiredDays = 0

  val certificateInfo = db.run(
   versionDBIO.getAllLearningPathWithValidPeriod
  )
  val currentDay = DateTime.now.withTimeAtStartOfDay()

  certificateInfo.map (
   _.map { case ((id, version), lp, status) =>
    val expirationDate = certificateTracker.getExpiredDate(status, version)
    expirationDate.map { date =>

     val days = Days.daysBetween(currentDay.withTimeAtStartOfDay(), date.withTimeAtStartOfDay()).getDays()
     if (expiredDays == days) {
      sendNotification(lp, status.userId, date)
      socialActivityHelper.addWithSet(lp.companyId,
       lp.userId,
       version.courseId,
       classPK = Some(lp.id),
       activityType = Some(CertificateActivitesType.Expired.id),
       createDate = DateTime.now)
     } else {
      version.expiringPeriod.foreach { p =>
       if (p.getDays == days)
        sendNotification(lp, status.userId, date)
      }
     }
    }
   }
  )
 }

 private def sendNotification(lp: LearningPath, userId: Long, date: DateTime) = {
  try {
   certificateTracker.sendNotification(lp.id, userId, date)(lp.companyId)
  } catch {
   case e: Exception => log.error(e)
  }
 }

}