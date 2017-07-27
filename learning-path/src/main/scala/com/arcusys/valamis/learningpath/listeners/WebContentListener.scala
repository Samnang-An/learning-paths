package com.arcusys.valamis.learningpath.listeners

import com.arcusys.valamis.learningpath.services.WebContentService
import com.arcusys.valamis.learningpath.tasks.TaskManager
import com.arcusys.valamis.learningpath.utils.{DbActions, DbActionsSupport}

import scala.concurrent.{ExecutionContext, Future}

class WebContentListener(val dbActions: DbActions,
                         taskManager: TaskManager,
                         webContentService: WebContentService)
                        (implicit executionContext: ExecutionContext)
  extends DbActionsSupport {

  def onWebContentViewed(userId: Long, webContentId: Long)
                        (implicit companyId: Long): Future[Unit] = {
    db.run {
      goalWebContentDBIO.getGoalIdsByWebContentId(webContentId)
    }
      .map {
        _.foreach { goalId =>
          taskManager.planWebContentGoalCheckerTask(goalId, userId, true)
        }
      }
  }

  def onWebContentViewedByClassPK(userId: Long, classPK: Long)
                        (implicit companyId: Long): Future[Unit] = {

    onWebContentViewed(userId,webContentService.getWebContentIdByClassPK(classPK))
  }
}
