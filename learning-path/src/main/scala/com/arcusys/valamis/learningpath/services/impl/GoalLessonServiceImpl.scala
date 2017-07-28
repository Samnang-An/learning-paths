package com.arcusys.valamis.learningpath.services.impl

import com.arcusys.valamis.learningpath.models.{Goal, GoalLesson, GoalTypes}
import com.arcusys.valamis.learningpath.services.exceptions.LessonsIsNotDeployedError
import com.arcusys.valamis.learningpath.services.{GoalLessonService, LessonService}
import com.arcusys.valamis.learningpath.utils.DbActions
import org.joda.time.Period

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

/**
  * Created by mminin on 26/01/2017.
  */
class GoalLessonServiceImpl(val dbActions: DbActions,
                            lessonService: LessonService)
                           (implicit val executionContext: ExecutionContext)
  extends GoalLessonService
    with GoalServiceBaseImpl {

  import profile.api._

  override val goalType = GoalTypes.Lesson

  override def create(learningPathId: Long,
                      timeLimit: Option[Period],
                      optional: Boolean,
                      lessonId: Long)
                     (implicit companyId: Long): Future[(Goal, GoalLesson, String)] = db.run {

    createGoalAction(learningPathId, timeLimit, optional) { goalId =>
      goalLessonDBIO.insert(GoalLesson(goalId, lessonId))
    } map { goal =>
      (goal, GoalLesson(goal.id, lessonId))
    } transactionally
  } flatMap toFullInfo

  override def createInGroup(parentGroupId: Long,
                             timeLimit: Option[Period],
                             optional: Boolean,
                             lessonId: Long)
                            (implicit companyId: Long): Future[(Goal, GoalLesson, String)] = db.run {
    createInGroupAction(parentGroupId, timeLimit, optional) { goalId =>
      goalLessonDBIO.insert(GoalLesson(goalId, lessonId))
    } map { goal =>
      (goal, GoalLesson(goal.id, lessonId))
    } transactionally
  } flatMap toFullInfo

  override def update(goalId: Long,
             timeLimit: Option[Period],
             optional: Boolean)
            (implicit companyId: Long): Future[(Goal, GoalLesson, String)] = db.run {
    updateAction(goalId, timeLimit, optional)(goalLessonDBIO.get(goalId))().transactionally
  } flatMap toFullInfo


  //TODO save title to db to avoid reading it from lesson service every time?
  private def toFullInfo(g: (Goal, GoalLesson)) = {
    val (goal, goalLesson) = g
    val lessonId = goalLesson.lessonId
    lessonService.getLessonNames(Seq(lessonId)).map { items =>
      (goal, goalLesson, items.getOrElse(lessonId, "Deleted lesson with id " + lessonId))
    } recover {
      case _: LessonsIsNotDeployedError => (goal, goalLesson, "Valamis is not deployed")
    }
  }
}
