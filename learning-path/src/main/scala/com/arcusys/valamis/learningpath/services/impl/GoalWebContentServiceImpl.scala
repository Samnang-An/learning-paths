package com.arcusys.valamis.learningpath.services.impl

import com.arcusys.valamis.learningpath.models.{Goal, GoalTypes, GoalWebContent}
import com.arcusys.valamis.learningpath.services.{GoalWebContentService, WebContentService}
import com.arcusys.valamis.learningpath.utils.DbActions
import org.joda.time.Period

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps


class GoalWebContentServiceImpl(val dbActions: DbActions,
                                webContentService: WebContentService
                           )
                               (implicit val executionContext: ExecutionContext)
  extends GoalWebContentService
    with GoalServiceBaseImpl {

  import profile.api._

  override val goalType = GoalTypes.WebContent

  override def create(learningPathId: Long,
                      timeLimit: Option[Period],
                      optional: Boolean,
                      webContentId: Long)
                     (implicit companyId: Long): Future[(Goal, GoalWebContent, String)] = db.run {

    createGoalAction(learningPathId, timeLimit, optional) { goalId =>
      goalWebContentDBIO.insert(GoalWebContent(goalId, webContentId))
    } map { goal =>
      (goal, GoalWebContent(goal.id, webContentId))
    } transactionally
  } flatMap toFullInfo

  override def createInGroup(parentGroupId: Long,
                             timeLimit: Option[Period],
                             optional: Boolean,
                             webContentId: Long)
                            (implicit companyId: Long): Future[(Goal, GoalWebContent, String)] = db.run {
    createInGroupAction(parentGroupId, timeLimit, optional) { goalId =>
      goalWebContentDBIO.insert(GoalWebContent(goalId, webContentId))
    } map { goal =>
      (goal, GoalWebContent(goal.id, webContentId))
    } transactionally
  } flatMap toFullInfo

  override def update(goalId: Long,
                      timeLimit: Option[Period],
                      optional: Boolean)
                     (implicit companyId: Long): Future[(Goal, GoalWebContent, String)] = db.run {
    updateAction(goalId, timeLimit, optional)(goalWebContentDBIO.get(goalId))().transactionally
  } flatMap toFullInfo


  private def toFullInfo(g: (Goal, GoalWebContent)) = {
    webContentService.getWebContentTitle(g._2.webContentId)
      .map(title => (g._1, g._2, title.getOrElse("Deleted webContent with id " +
        g._2.webContentId)))
  }
}