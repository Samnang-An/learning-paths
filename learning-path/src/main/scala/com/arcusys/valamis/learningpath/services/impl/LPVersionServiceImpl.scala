package com.arcusys.valamis.learningpath.services.impl

import com.arcusys.valamis.learningpath.models._
import com.arcusys.valamis.learningpath.services.exceptions.NoLearningPathError
import com.arcusys.valamis.learningpath.services.impl.actions.{ImprovingCompetenceDBIOActions, RecommendedCompetenceDBIOActions}
import com.arcusys.valamis.learningpath.services.impl.utils.{CopyCompetencesSupport, CopyGoalsSupport}
import com.arcusys.valamis.learningpath.services.{FileStorage, LPVersionService}
import com.arcusys.valamis.learningpath.utils.{DbActions, DbActionsSupport}
import com.arcusys.valamis.members.picker.service.MemberService
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by mminin on 26/01/2017.
  */
class LPVersionServiceImpl(val dbActions: DbActions,
                           val improvingCompetenceDBIO: ImprovingCompetenceDBIOActions,
                           val recommendedCompetenceDBIO: RecommendedCompetenceDBIOActions,
                           logoFileStorage: FileStorage,
                           memberService: MemberService)
                          (implicit val executionContext: ExecutionContext)
  extends LPVersionService
    with DbActionsSupport
    with CopyGoalsSupport
    with CopyCompetencesSupport {

  import profile.api._

  //TODO: try to simplify code
  def createNewDraft(learningPathId: Long)
                    (implicit companyId: Long): Future[(LearningPath, LPVersion)] = {
    val now = DateTime.now

    val action = for {
      learningPath <- learningPathDBIO.getById(learningPathId)
        .map(_.getOrElse(throw new NoLearningPathError(learningPathId)))
      (versionId, version) <- versionDBIO.getLastByLearningPathId(learningPathId)
        .map(_.getOrElse(throw new NoLearningPathError(learningPathId)))
        .flatMap { case (versionId, version) =>
          if (!version.published) {
            DBIO.successful((versionId, version))
          } else {
            createNewDraft(versionId, version, now)
          }
        }
    } yield {
      (learningPath, version)
    }

    db.run(action.transactionally)
  }

  private def createNewDraft(versionId: Long,
                             version: LPVersion,
                             now: DateTime
                            ): DBIO[(Long, LPVersion)] = {
    val newVersion = version.copy(published = false, createdDate = now, modifiedDate = now)

    for {
      newVersionId <- versionDBIO.insert(newVersion)
      _ <- learningPathDBIO.updateHasDraft(version.learningPathId, hasDraft = true)
      _ <- copyGoals(versionId, newVersionId, now)
      _ <- copyCompetences(versionId, newVersionId)
    } yield {
      (newVersionId, newVersion)
    }
  }

  protected def createNewGoalVersion(newVersionId: Long,
                                   groupId: Option[Long],
                                   goal: Goal,
                                   now: DateTime): DBIO[Long] = {
    goalDBIO.insert(
      oldGoalId = Some(goal.id),
      versionId = newVersionId,
      groupId = groupId,
      goalType = goal.goalType,
      indexNumber = goal.indexNumber,
      timeLimit = goal.timeLimit,
      optional = goal.optional,
      now = now
    )
  }
}
