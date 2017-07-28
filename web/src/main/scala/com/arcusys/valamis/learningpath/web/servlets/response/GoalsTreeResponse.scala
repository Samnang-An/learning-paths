package com.arcusys.valamis.learningpath.web.servlets.response

import com.arcusys.valamis.learningpath.models.{GoalTypes, GoalsSet}
import org.joda.time.{DateTime, Period}

/**
  * Created by mminin on 21/02/2017.
  */
case class TreeNode(id: Long,
                    groupId: Option[Long],
                    indexNumber: Int,
                    timeLimit: Option[Period],
                    optional: Boolean,
                    modifiedDate: DateTime,
                    title: String,
                    count: Option[Int],
                    goals: Seq[GoalResponse],
                    goalType: GoalTypes.Value = GoalTypes.Group
                   ) extends GoalResponse {
  def this(group: GoalsGroupResponse, goals: Seq[GoalResponse]) = this(
    group.id,
    group.groupId,
    group.indexNumber,
    group.timeLimit,
    group.optional,
    group.modifiedDate,
    group.title,
    group.count,
    goals
  )
}

object GoalsTreeBuilder {
  def build(allGoals: GoalsSet): Seq[GoalResponse] = {
    getGroupElements(parentGroupId = None, allGoals)
  }

  def getGroupElements(parentGroupId: Option[Long], allGoals: GoalsSet): Seq[GoalResponse] = {
    val groups = allGoals.groups
      .filter(_._1.groupId == parentGroupId)
      .map(GoalsGroupResponse(_))
      .map(g => new TreeNode(g, getGroupElements(Some(g.id), allGoals)))

    val lrActivities = allGoals.lrActivities
      .filter(_._1.groupId == parentGroupId)
      .map(GoalLRActivityResponse(_))

    val lessons = allGoals.lessons
      .filter(_._1.groupId == parentGroupId)
      .map(GoalLessonResponse(_))

    val assignments = allGoals.assignments
      .filter(_._1.groupId == parentGroupId)
      .map(GoalAssignmentResponse(_))

    val webContents = allGoals.webContents
      .filter(_._1.groupId == parentGroupId)
      .map(GoalWebContentResponse(_))

    val trainingEvents = allGoals.trainingEvents
      .filter(_._1.groupId == parentGroupId)
      .map(GoalTrainingEventResponse(_))

    val statements = allGoals.statements
      .filter(_._1.groupId == parentGroupId)
      .map(GoalStatementResponse(_))

    val courses = allGoals.courses
      .filter(_._1.groupId == parentGroupId)
      .map(GoalCourseResponse(_))

    (groups ++ lrActivities ++ lessons ++ assignments ++
      webContents ++ trainingEvents ++ statements ++ courses)
      .sortBy(_.indexNumber)
  }
}

