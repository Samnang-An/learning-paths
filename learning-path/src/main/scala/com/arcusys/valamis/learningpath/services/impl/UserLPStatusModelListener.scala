package com.arcusys.valamis.learningpath.services.impl

import com.arcusys.valamis.learningpath.models.UserLPStatus
import com.arcusys.valamis.learningpath.models.history.UserStatusSnapshot
import com.arcusys.valamis.learningpath.utils.{DbActions, DbActionsSupport}
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by mminin on 05/04/2017.
  */
class UserLPStatusModelListener(val dbActions: DbActions)
                               (implicit executionContext: ExecutionContext)
  extends DbActionsSupport {


  private[learningpath] def onCreated(userLPStatuses: Seq[UserLPStatus]): Future[Unit] = {
    addSnapshotsToHistory(userLPStatuses)
      .map(_ => Unit)
  }

  private[learningpath] def onChanged(userLPStatus: UserLPStatus): Future[Unit] = {
    addSnapshotToHistory(userLPStatus)
      .map(_ => Unit)
  }

  private[learningpath] def onChanged(userLPStatuses: Seq[UserLPStatus]): Future[Unit] = {
    addSnapshotsToHistory(userLPStatuses)
      .map(_ => Unit)
  }

  private[learningpath] def onDeleted(userLPStatus: UserLPStatus): Future[Unit] = {
    addSnapshotToHistory(userLPStatus, deleted = true)
      .map(_ => Unit)
  }

  private[learningpath] def onDeleted(userLPStatuses: Seq[UserLPStatus]): Future[Unit] = {
    addSnapshotsToHistory(userLPStatuses, deleted = true)
      .map(_ => Unit)
  }

  private def addSnapshotsToHistory(userLPStatuses: Seq[UserLPStatus],
                                    deleted: Boolean = false) = {

    db.run {
      historyDBIOActions
        .insertUserStatusSnapshots(userLPStatuses.map(convert(_, deleted)))
    }
  }

  private def addSnapshotToHistory(userLPStatus: UserLPStatus,
                                   deleted: Boolean = false) = {

    db.run {
      historyDBIOActions
        .insertUserStatusSnapshot(convert(userLPStatus, deleted))
    }
  }

  private def convert(userLPStatus: UserLPStatus, deleted: Boolean = false) = {
    UserStatusSnapshot(
      userLPStatus.learningPathId,
      userLPStatus.userId,
      userLPStatus.status,
      DateTime.now(),
      deleted
    )
  }
}
