package com.arcusys.valamis.learningpath.services.impl

import com.arcusys.valamis.learningpath.models._
import com.arcusys.valamis.learningpath.services.exceptions._
import com.arcusys.valamis.learningpath.services._
import com.arcusys.valamis.learningpath.utils.{DbActions, DbActionsSupport}
import com.arcusys.valamis.lrs.api.FailureRequestException
import com.arcusys.valamis.lrssupport.lrs.service.LrsClientManager
import org.apache.commons.logging.Log
import org.joda.time.DateTime
import org.json4s.jackson.JsonMethods

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}
import org.json4s.{DefaultFormats, Extraction, Formats}

/**
  * Created by mminin on 26/01/2017.
  */
class GoalServiceImpl(val dbActions: DbActions,
                      lessonService: LessonService,
                      webContentService: WebContentService,
                      courseService: CourseService,
                      trainingEventServiceBridge: TrainingEventServiceBridge,
                      lrsClient: LrsClientManager,
                      val messageBusService: MessageBusService)
                     (implicit executionContext: ExecutionContext, log: Log)
  extends GoalService
    with AssignmentSupport
    with DbActionsSupport
    with JsonMethods {

  import profile.api._

  override def get(id: Long)
                  (implicit companyId: Long): Future[Option[Goal]] = {
    db.run(goalDBIO.get(id))
  }

  override def getGoalsByLPCurrentVersion(learningPathId: Long)
                           (implicit companyId: Long): Future[GoalsSet] = {
    db.run(learningPathDBIO.getById(learningPathId))
      .map(_.getOrElse(throw new NoLearningPathError(learningPathId)))
      .flatMap { learningPath =>
        getAllGoals(learningPath.currentVersionId.get)
      }
  }


  override def getGoalsByLPDraftVersion(learningPathId: Long)
                                (implicit companyId: Long): Future[GoalsSet] = {

    db.run(versionDBIO.getDraftByLearningPathId(learningPathId))
      .map(_.getOrElse(throw new NoLearningPathDraftError(learningPathId)))
      .flatMap { case (versionId, _) =>
        getAllGoals(versionId)
      }
  }


  override def getGoalsByVersion(versionId: Long)
                                (implicit companyId: Long): Future[GoalsSet] = {
    db.run(versionDBIO.getById(versionId))
      .flatMap {
        case None => throw new NoVersionError(versionId)
        case Some((id, _)) => getAllGoals(versionId)
      }
  }


  private def getAssignmentGoals(versionId: Long) = {
    db.run(goalAssignmentDBIO.getWithGoalInfoByVersionId(versionId)) map { goals =>
      val assignmentIds = goals map { case (_, goalData) => goalData.assignmentId }

      val assignments: Map[Long, Assignment] = getAssignmentByIds(assignmentIds) match {
        case Success(items) => items.groupBy(_.id) mapValues (_.head) withDefault { id =>
          Assignment(id, "Deleted assignment with id " + id, "", None)
        }
        case Failure(_: AssignmentIsNotDeployedError) => Map() withDefault { id =>
          Assignment(id, "Assignments is not deployed", "", None)
        }
        case Failure(ex) => throw ex
      }

      goals map { case (goal, goalData) =>
        (goal, goalData, assignments(goalData.assignmentId).title)
      }

    }
  }

  private def getLessonGoals(versionId: Long) = {
    db.run(goalLessonDBIO.getWithGoalInfoByVersionId(versionId)) flatMap { goals =>
      val lessonIds = goals map { case (_, goalData) => goalData.lessonId }

      lessonService.getLessonNames(lessonIds) map { lessons =>
        lessons withDefault { id => "Deleted lesson with id " + id }
      } recover {
        case _: LessonsIsNotDeployedError =>
          Map[Long, String]().withDefaultValue("Valamis is not deployed")
      } map { lessons =>
        goals map { case (goal, goalData) =>
          (goal, goalData, lessons(goalData.lessonId))
        }
      }
    }
  }

  private def getCourseGoals(versionId: Long) = {
    db.run(goalCourseDBIO.getWithGoalInfoByVersionId(versionId)) flatMap { goals =>
      val courseIds = goals map { case (_, goalData) => goalData.courseId }

      courseService.getCourseTitlesByIds(courseIds) map { courseTitles =>
        courseTitles withDefault { id => "Deleted course with id " + id }
      } map { courseTitles =>
        goals map { case (goal, goalData) =>
          (goal, goalData, courseTitles(goalData.courseId))
        }
      }
    }
  }

  private def getStatementGoals(versionId: Long)(implicit companyId: Long) =
    db.run {
      goalStatementDBIO.getWithGoalInfoByVersionId(versionId) flatMap { goals =>
        DBIO.sequence(goals map { case (goal, statementGoal) =>
          if (statementGoal.objectName.isEmpty) {
            getObjectName(statementGoal.objectId) match {
              case Success(objectName) =>
                goalStatementDBIO.updateObjectName(goal.id, objectName) map { _ =>
                  (goal, statementGoal.copy(objectName = objectName))
                }
              case Failure(_) =>
                //will try again next time statement goals is requested
                DBIO.successful((goal, statementGoal))
            }

          } else DBIO.successful((goal, statementGoal))
        })
      }
    }

  private def getAllGoals(versionId: Long)(implicit companyId: Long): Future[GoalsSet] = {
    val groupsF = db.run(goalGroupDBIO.getWithGoalInfoByVersionId(versionId))

    val lrActivitiesF = db.run(goalLRActivityDBIO.getWithGoalInfoByVersionId(versionId))
    val assignmentsF = getAssignmentGoals(versionId)
    val lessonsF = getLessonGoals(versionId)
    val webContentsF = getWebContentsGoals(versionId)
    val trainingEventsF = getTrainingEventsGoals(versionId)
    val statementsF = getStatementGoals(versionId)
    val coursesF = getCourseGoals(versionId)

    for {
      groups <- groupsF
      lessons <- lessonsF
      lrActivities <- lrActivitiesF
      assignments <- assignmentsF
      webContents <- webContentsF
      trainingEvents <- trainingEventsF
      statements <- statementsF
      courses <- coursesF
    } yield {
      GoalsSet(groups, lessons, lrActivities, assignments,
        webContents, trainingEvents, statements, courses)
    }
  }

  def delete(goalId: Long)
            (implicit companyId: Long): Future[Unit] = {
    val now = DateTime.now()

    val action = for {
      goal <- goalDBIO.get(goalId) map {
        _.getOrElse(throw new NoGoalError(goalId))
      }
      _ <- versionDBIO.isPublishedById(goal.versionId).map(_.collect {
        case true => throw new VersionPublishedError
      })
      _ <- goal.goalType match {
        case GoalTypes.LRActivity => goalActivityDBIO.delete(goalId)
        case GoalTypes.Lesson => goalLessonDBIO.delete(goalId)
        case GoalTypes.Assignment => goalAssignmentDBIO.delete(goalId)
        case GoalTypes.TrainingEvent => goalTrainingEventDBIO.delete(goalId)
        case GoalTypes.Statement => goalStatementDBIO.delete(goalId)
        case GoalTypes.Course => goalCourseDBIO.delete(goalId)
        case GoalTypes.WebContent => goalWebContentDBIO.delete(goalId)

      }
      _ <- goalDBIO.delete(goalId)
      _ <- versionDBIO.updateModifiedDate(goal.versionId, now)
    } yield {}

    db.run(action.transactionally)
  }

  //TODO: test transactions with high load
  //TODO: use goal link to previous goal instead numbers ?
  def move(goalId: Long, newGroupId: Option[Long], newIndexNumber: Int)
          (implicit companyId: Long): Future[Unit] = {
    val now = DateTime.now()

    val action = for {
      goal <- goalDBIO.get(goalId).map(_.getOrElse(throw new NoGoalError(goalId)))

      _ <- validateDraftVersion(goal.versionId)
      _ <- validateMoving(goal, newGroupId)

      // if we move down we should add 1 to newIndexNumber,
      // otherwise move from 0 to 1 will do nothing
      goalIndexNumber = if (goal.groupId == newGroupId && newIndexNumber > goal.indexNumber){
        newIndexNumber + 1
      } else {
        newIndexNumber
      }

      nextGoals <- goalDBIO.getByVersionIdAndParentGroupId(goal.versionId, newGroupId)
        .map(_.filter(_.indexNumber >= goalIndexNumber))
      _ <- updateIndexNumbers(nextGoals, 1 + goalIndexNumber)

      _ <- goalDBIO.updateGroupAndIndexNumber(goalId, newGroupId, goalIndexNumber)

      oldGroupGoals <- goalDBIO.getByVersionIdAndParentGroupId(goal.versionId, goal.groupId)
      _ <- updateIndexNumbers(oldGroupGoals)

      _ <- versionDBIO.updateModifiedDate(goal.versionId, now)
    } yield {}

    db.run(action.transactionally)
  }

  private def getWebContentsGoals(versionId: Long) = {
    db.run(goalWebContentDBIO.getWithGoalInfoByVersionId(versionId)) flatMap { webContents =>
      Future.sequence(webContents map { case (goal, goalData) =>
        webContentService.getWebContentTitle(goalData.webContentId) map { title =>
          (goal, goalData, title.getOrElse("Deleted webContent with id "
            + goalData.webContentId))
        }
      })
    }
  }

  private def getTrainingEventsGoals(versionId: Long) = {
    db.run(goalTrainingEventDBIO.getWithGoalInfoByVersionId(versionId))
      .flatMap(trainingEvents => Future.sequence(trainingEvents.map {
        case (goal, goalData) =>
          //TODO create TrainingService to get real training event title
          trainingEventServiceBridge.getEventTitle(goalData.trainingEventId) map { title =>
            (goal, goalData, title.getOrElse("Deleted event with id " + goalData.trainingEventId))
          }
      }))
  }

  private def updateIndexNumbers(goals: Seq[Goal],
                                 offset: Int = 0): DBIO[Unit] = {
    DBIO.seq(goals
      .sortBy(_.indexNumber)
      .zipWithIndex
      .filter { case (g, i) => g.indexNumber != offset + i }
      .map { case (g, i) => goalDBIO.updateIndexNumber(g.id, offset + i) }: _*)
  }


  private def getTreeSize(groupId: Option[Long], groups: Seq[Goal]): Int = {
    groups
      .filter(_.groupId == groupId)
      .map(g => 1 + getTreeSize(Some(g.id), groups))
      .sum
  }

  private def validateDraftVersion(versionId: Long): DBIO[Unit] = {
    versionDBIO.isPublishedById(versionId).map(_.collect {
      case true => throw new VersionPublishedError
    })
  }

  private def validateMoving(goal: Goal,
                             newGroupIdOpt: Option[Long]): DBIO[_] = {
    newGroupIdOpt match {
      case None => DBIO.successful(Unit)
      case Some(newGroupId) =>
        if (goal.goalType != GoalTypes.Group) {
          validateNewParent(goal, newGroupId)
        } else {
          validateNewParent(goal, newGroupId).flatMap { _ =>
            validateNewGroupParent(goal, newGroupId)
          }
        }
    }
  }

  private def validateNewParent(goal: Goal, newGroupId: Long): DBIO[_] = {
    goalDBIO.get(newGroupId) map {
      case Some(newParent) if newParent.goalType == GoalTypes.Group
        && newParent.versionId == goal.versionId => Unit
      case _ => throw new InvalidGoalsTreeError
    }
  }

  private def validateNewGroupParent(goal: Goal, newGroupId: Long) = {
    goalDBIO.getByVersionIdAndType(goal.versionId, GoalTypes.Group)
      .map { groups =>
        val updateGroups = goal.copy(groupId = Some(newGroupId)) +: groups.filter(_.id != goal.id)
        if (groups.size != getTreeSize(None, updateGroups)) throw new InvalidGoalsTreeError
      }
  }


  private def getObjectName(objId: String)(implicit companyId: Long): Try[String] = {
    lrsClient.activityApi(_.getActivity(objId)) match {
      case Success(activity) =>
        Success(activity.name map (name => toJson(name)) getOrElse "Activity without name")
      case Failure(ex: FailureRequestException) if ex.responseCode == 404 => Success("Deleted activity")
      case Failure(ex) => Failure(ex)
    }
  }

  private def toJson[T](obj: T)(implicit formats: Formats = DefaultFormats): String = compact(render(Extraction.decompose(obj)))
}
