package com.arcusys.valamis.learningpath.listeners.competences.messages

import com.arcusys.valamis.learningpath.models.Competence

/**
  * Created by pkornilov on 6/21/17.
  */
case class CompetencesImprovedMessage(
 userId: Long,
 learningPathTitle: String,
 competences: Seq[Competence],
 lpActivityId: String,
 lpCompletedStatementId: Option[String]
)
