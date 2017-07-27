package com.arcusys.valamis.learningpath.listeners

import com.arcusys.valamis.learningpath.listeners.competences.messages.ItemChangedMessage
import com.arcusys.valamis.learningpath.listeners.competences.{LevelChangeListener, SkillChangeListener}
import com.arcusys.valamis.learningpath.models.CompetenceMessageActions
import com.arcusys.valamis.learningpath.utils.JsonHelper
import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase
import com.arcusys.valamis.message.broker.MessageListener

/**
  * Created by pkornilov on 6/16/17.
  */
class CompetenceChangeListenerTest extends LPServletTestBase {

  import scala.concurrent.ExecutionContext.Implicits.global

  private val competence1 = testCompetence(1, 11)
  private val competence2 = testCompetence(2, 22)
  private val competence3 = testCompetence(3, 33)
  private val competence4 = testCompetence(4, 44)
  private val competence5 = testCompetence(5, 55)

  private implicit val companyId = -1L

  lazy val competencesLevelChangeListener = new LevelChangeListener(
    servlet.recommendedCompetenceService, servlet.improvingCompetenceService
  )

  lazy val competencesSkillChangeListener = new SkillChangeListener(
    servlet.recommendedCompetenceService, servlet.improvingCompetenceService
  )

  test("should change level name in related LP when it's changed in Competences Admin") {
    createTestData()
    sendTestMessage(
      listener = competencesLevelChangeListener,
      action = CompetenceMessageActions.LevelChanged,
      id = 11,
      name = "Changed level 11 name")
    checkLevelChanges()
  }

  test("should change level name in related LP when it's deleted in Competences Admin") {
    createTestData()
    sendTestMessage(
      listener = competencesLevelChangeListener,
      action = CompetenceMessageActions.LevelDeleted,
      id = 11,
      name = "Changed level 11 name")
    checkLevelChanges()
  }

  test("should change skill name in related LP when it's changed in Competences Admin") {
    createTestData()
    sendTestMessage(
      listener = competencesSkillChangeListener,
      action = CompetenceMessageActions.SkillChanged,
      id = 1,
      name = "Changed skill 1 name")
    checkSkillChanges()
  }

  test("should change skill name in related LP when it's deleted in Competences Admin") {
    createTestData()
    sendTestMessage(
      listener = competencesSkillChangeListener,
      action = CompetenceMessageActions.SkillDeleted,
      id = 1,
      name = "Changed skill 1 name")
    checkSkillChanges()
  }

  private def checkLevelChanges(): Unit = {
    val (lp1Id, lp2Id) = (1, 2)
    get(s"/learning-paths/$lp1Id/draft/$recommendedCompetencesPath") {
      status should beOk
      body should haveJson(
        s"""[
           |{
           |  "skillId": 1,
           |  "skillName": "Skill 1",
           |  "levelId": 11,
           |  "levelName": "Changed level 11 name"
           |},
           | $competence3
           |]""".stripMargin,
        strict = true
      )
    }

    get(s"/learning-paths/$lp1Id/draft/$improvingCompetencesPath") {
      status should beOk
      body should haveJson(
        s"""[
           |{
           |  "skillId": 1,
           |  "skillName": "Skill 1",
           |  "levelId": 11,
           |  "levelName": "Changed level 11 name"
           |},
           | $competence4
           |]""".stripMargin,
        strict = true
      )
    }

    get(s"/learning-paths/$lp2Id/draft/$recommendedCompetencesPath") {
      status should beOk
      body should haveJson(
        s"""[
           |{
           |  "skillId": 1,
           |  "skillName": "Skill 1",
           |  "levelId": 11,
           |  "levelName": "Changed level 11 name"
           |},
           | $competence2
           |]""".stripMargin,
        strict = true
      )
    }

    get(s"/learning-paths/$lp2Id/draft/$improvingCompetencesPath") {
      status should beOk
      body should haveJson(
        s"""[
           |{
           |  "skillId": 1,
           |  "skillName": "Skill 1",
           |  "levelId": 11,
           |  "levelName": "Changed level 11 name"
           |},
           | $competence5
           |]""".stripMargin,
        strict = true
      )
    }
  }

  private def checkSkillChanges(): Unit = {
    val (lp1Id, lp2Id) = (1, 2)
    get(s"/learning-paths/$lp1Id/draft/$recommendedCompetencesPath") {
      status should beOk
      body should haveJson(
        s"""[
           |{
           |  "skillId": 1,
           |  "skillName": "Changed skill 1 name",
           |  "levelId": 11,
           |  "levelName": "Level 11"
           |},
           | $competence3
           |]""".stripMargin,
        strict = true
      )
    }

    get(s"/learning-paths/$lp1Id/draft/$improvingCompetencesPath") {
      status should beOk
      body should haveJson(
        s"""[
           |{
           |  "skillId": 1,
           |  "skillName": "Changed skill 1 name",
           |  "levelId": 11,
           |  "levelName": "Level 11"
           |},
           | $competence4
           |]""".stripMargin,
        strict = true
      )
    }

    get(s"/learning-paths/$lp2Id/draft/$recommendedCompetencesPath") {
      status should beOk
      body should haveJson(
        s"""[
           |{
           |  "skillId": 1,
           |  "skillName": "Changed skill 1 name",
           |  "levelId": 11,
           |  "levelName": "Level 11"
           |},
           | $competence2
           |]""".stripMargin,
        strict = true
      )
    }

    get(s"/learning-paths/$lp2Id/draft/$improvingCompetencesPath") {
      status should beOk
      body should haveJson(
        s"""[
           |{
           |  "skillId": 1,
           |  "skillName": "Changed skill 1 name",
           |  "levelId": 11,
           |  "levelName": "Level 11"
           |},
           | $competence5
           |]""".stripMargin,
        strict = true
      )
    }
  }

  private def createTestData() = {
    val lp1Id = createLearningPath("test lp 1")
    addCompetenceToLPDraft(lp1Id, recommendedCompetencesPath, competence1)
    addCompetenceToLPDraft(lp1Id, recommendedCompetencesPath, competence3)
    addCompetenceToLPDraft(lp1Id, improvingCompetencesPath, competence1)
    addCompetenceToLPDraft(lp1Id, improvingCompetencesPath, competence4)

    val lp2Id = createLearningPath("test lp 2")
    addCompetenceToLPDraft(lp2Id, recommendedCompetencesPath, competence1)
    addCompetenceToLPDraft(lp2Id, recommendedCompetencesPath, competence2)
    addCompetenceToLPDraft(lp2Id, improvingCompetencesPath, competence1)
    addCompetenceToLPDraft(lp2Id, improvingCompetencesPath, competence5)
  }

  private def sendTestMessage(listener: MessageListener,
                              action: String,
                              id: Long,
                              name: String)
                             (implicit companyId: Long): Unit = {
    val data = JsonHelper.toJson(ItemChangedMessage(
      action = action,
      id = id,
      name = name
    ))
    listener.processMessage(data)
  }

}
