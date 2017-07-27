package com.arcusys.valamis.learningpath.tasks

import com.arcusys.valamis.learningpath.utils.{DbActions, DbActionsSupport}
import org.joda.time.DateTime

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

/**
  * Created by pkornilov on 4/5/17.
  */
class DeadlineChecker(val dbActions: DbActions,
                      expiredGoalChecker: ExpiredGoalsChecker)
                     (implicit executionContext: ExecutionContext)
  extends DbActionsSupport {

  def checkDeadlines(now: DateTime = DateTime.now): Future[Unit] = {
    db.run(learningPathDBIO.selectAllActive) flatMap { lps =>
      Future.sequence(lps map { lp =>
        expiredGoalChecker.check(lp.currentVersionId.get, now)(lp.companyId)
      }) map (_ => ())
    }
  }

}
