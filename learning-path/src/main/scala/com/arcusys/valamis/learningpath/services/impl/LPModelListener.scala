package com.arcusys.valamis.learningpath.services.impl

import com.arcusys.valamis.learningpath.models.history.LPSnapshot
import com.arcusys.valamis.learningpath.models.{LPVersion, LearningPath}
import com.arcusys.valamis.learningpath.services.{AssetService, MessageBusService}
import com.arcusys.valamis.learningpath.utils.{DbActions, DbActionsSupport, PeriodHelper}
import org.apache.commons.logging.Log
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by mminin on 05/04/2017.
  */
class LPModelListener(val dbActions: DbActions,
                      assetService: AssetService,
                      messageBusService: MessageBusService,
                      log: Log)
                     (implicit executionContext: ExecutionContext)
  extends DbActionsSupport {


  private[services] def onDeleted(learningPath: LearningPath, version: LPVersion): Future[Unit] = {
    sendDeletedMessage(learningPath.id)

    val deleteAssetF = assetService.delete(learningPath, version)
    val createSnapshotF = addSnapshotToHistory(learningPath, version, deleted = true)

    createSnapshotF flatMap (_ => deleteAssetF)
  }

  private def sendDeletedMessage(lpId: Long) = {
    try {
      val messageValues = new java.util.HashMap[String, AnyRef]()
      messageValues.put("learningPathId", lpId.toString)
      messageBusService.sendAsynchronousMessage("valamis/learningPaths/deleted", messageValues)
    } catch {
      case ex: Throwable =>
        log.error(s"Failed to send learning path deleted event " +
          s"via MessageBus for learningPathId $lpId", ex)
    }
  }

  private[services] def onCreated(learningPath: LearningPath, version: LPVersion): Future[Unit] = {
    val createAssetF = assetService.create(learningPath, version)
    val createSnapshotF = addSnapshotToHistory(learningPath, version)

    createSnapshotF flatMap (_ => createAssetF)
  }

  private[services] def onChanged(learningPath: LearningPath, version: LPVersion): Future[Unit] = {
    val updateAssetF = assetService.update(learningPath, version)
    val createSnapshotF = addSnapshotToHistory(learningPath, version)

    createSnapshotF flatMap (_ => updateAssetF)
  }

  private def addSnapshotToHistory(learningPath: LearningPath,
                                   version: LPVersion,
                                   deleted: Boolean = false) = {
    val (periodType, periodValue) = version.validPeriod
      .flatMap(PeriodHelper.toValamisPeriod(_).toOption)
      .getOrElse(PeriodHelper.emptyPeriod)

    db.run {
      historyDBIOActions.insertLPSnapshot(
        LPSnapshot(
          learningPath.id,
          DateTime.now,
          deleted,
          version.title,
          version.expiringPeriod.isEmpty,
          learningPath.companyId,
          periodType,
          periodValue,
          version.published && learningPath.activated,
          version.courseId
        ))
    }
  }
}
