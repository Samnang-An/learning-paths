package com.arcusys.valamis.learningpath.services.impl.tables

import com.arcusys.valamis.learningpath.services.impl.tables.competences.CompetencesTableComponent
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickProfile

/**
  * Created by mminin on 01/02/2017.
  */
trait LearningPathTables extends LeaningPathTableComponent
  with LPVersionTableComponent
  with GoalTableComponent
  with GoalGroupTableComponent
  with GoalLessonTableComponent
  with GoalAssignmentTableComponent
  with GoalLRActivityTableComponent
  with GoalStatementTableComponent
  with GoalCourseTableComponent
  with LPMemberTableComponent
  with UserGoalStatusTableComponent
  with UserLPStatusTableComponent
  with GoalWebContentTableComponent
  with GoalTrainingEventTableComponent
  with CompetencesTableComponent {
  _: SlickProfile =>
}
