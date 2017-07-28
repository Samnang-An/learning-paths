package com.arcusys.valamis.learningpath.web.servlets.utils

import com.arcusys.valamis.learningpath.ServletImpl
import com.arcusys.valamis.learningpath.listeners.LRActivityListener
import com.arcusys.valamis.learningpath.messaging.model.CommonMessageFields
import com.arcusys.valamis.learningpath.models.{GoalStatuses, GoalTypes, StatementInfo, UserGoalStatus}
import com.arcusys.valamis.learningpath.utils.JsonHelper
import com.arcusys.valamis.members.picker.model.MemberTypes
import org.joda.time.{DateTime, Period}
import org.json4s.{DefaultFormats, Formats}
import org.json4s.ext.{DateTimeSerializer, EnumNameSerializer, PeriodSerializer}
import org.json4s.jackson.JsonMethods._
import org.scalatra.test.scalatest.ScalatraSuite

import scala.concurrent.Future

/**
  * Created by mminin on 27/01/2017.
  */
trait TestHelper extends ResponseMatcher with JsonMatcher {
  self: ScalatraSuite =>

  def await[T](f: Future[T]): T

  implicit val jsonFormat: Formats = DefaultFormats + DateTimeSerializer + PeriodSerializer +
    new EnumNameSerializer(GoalStatuses) + new EnumNameSerializer(GoalTypes)

  val jsonContentHeaders = Map("Content-Type" -> "application/json; charset=UTF-8")

  val recommendedCompetencesPath = "recommended-competences"
  val improvingCompetencesPath = "improving-competences"

  def createLearningPath(title: String,
                         courseId: Option[Long] = None,
                         headers: Map[String, String] = Map()): Long = {
    val (lpId, _) = createLearningPathAndGetIds(title, courseId, headers)
    lpId
  }

  //TODO: improve naming, remove 'createLearningPath'
  def createLearningPathAndGetIds(title: String,
                         courseId: Option[Long] = None,
                         headers: Map[String, String] = Map()): (Long, Long) = {
    post("/learning-paths",
      s"""{
         |  "title": "$title",
         |  "courseId": ${courseId.getOrElse("null")}
         |} """.stripMargin,
      jsonContentHeaders ++ headers
    ) {
      status should beOk

      val id = (parse(body) \ "id").extract[Long]
      val versionId = (parse(body) \ "currentVersionId").extract[Long]
      (id, versionId)
    }
  }

  def publish(id: Long, headers: Map[String, String] = Map()): Unit = {
    post(s"/learning-paths/$id/draft/publish/", headers = headers) {
      status should beNoContent
    }
  }

  def cloneLearningPath(id: Long, headers: Map[String, String] = Map()): Long = {
    post(s"/learning-paths/$id/clone", headers = headers) {
      status should beOk
      (parse(body) \ "id").extract[Long]
    }
  }

  def deleteLearningPath(id: Long, headers: Map[String, String] = Map()): Unit = {
    delete(s"/learning-paths/$id", headers = headers) {
      status should beNoContent
    }
  }


  def deactivate(id: Long): Unit = {
    post(s"/learning-paths/$id/deactivate") {
      status should beNoContent
    }
  }

  def activate(id: Long): Unit = {
    post(s"/learning-paths/$id/activate") {
      status should beNoContent
    }
  }

  def updateGoal(goalId: Long, goalType: GoalTypes.Value, timeLimit: Option[Period], optional: Boolean = false): Unit = {
    put(s"/goals/$goalId",
      body = JsonHelper.toJson(Map(
        "goalType" -> goalType,
        "timeLimit" -> timeLimit,
        "optional" -> optional
      )),
      headers = jsonContentHeaders) {
      status should beOk
    }

  }

  def createNewDraft(id: Long): Unit = {
    post(s"/learning-paths/$id/draft") {
      status should beOk
    }
  }

  def setLogoToDraft(lpId: Long, bytes: Array[Byte], format: String = "png",
                     headers: Map[String, String] = Map()): String = {
    put(
      uri = s"/learning-paths/$lpId/draft/logo",
      body = bytes,
      headers = Map(
        "Content-Type" -> s"image/$format",
        "Content-Disposition" -> s"""attachment; filename="my.$format""""
      ) ++ headers
    ) {
      status should beOk

      (parse(body) \ "logoUrl").extract[String]
    }
  }

  def createGoalGroup(learningPathId: Long, title: String): Long = {
    post(s"/learning-paths/$learningPathId/draft/groups/",
      s"""{ "title": "$title" }""",
      jsonContentHeaders
    ) {
      status should beOk

      (parse(body) \ "id").extract[Long]
    }
  }

  def createSubGoalGroup(parentGroupId: Long, title: String, count: Option[Int] = None): Long = {
    post(s"/goal-groups/$parentGroupId/groups/",
      s"""{ "title": "$title", "count": ${count.getOrElse("null")} }""",
      jsonContentHeaders
    ) {
      status should beOk

      (parse(body) \ "id").extract[Long]
    }
  }

  def deleteGoal(goalId: Long): Unit = {
    delete(s"/goals/$goalId") {
      status should beNoContent
    }
  }

  def createLessonGoal(learningPathId: Long, lessonId: Long,
                       headers: Map[String, String] = Map()): Long = {
    post(s"/learning-paths/$learningPathId/draft/goals/",
      s"""{
         |  "goalType": "lesson",
         |  "lessonId": $lessonId
         |}""".stripMargin,
      jsonContentHeaders ++ headers
    ) {
      status should beOk

      (parse(body) \ "id").extract[Long]
    }
  }

  def createAssignmentGoal(learningPathId: Long, assignmentId: Long): Long = {
    post(s"/learning-paths/$learningPathId/draft/goals/",
      s"""{
         |  "goalType": "assignment",
         |  "assignmentId": $assignmentId
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beOk

      (parse(body) \ "id").extract[Long]
    }
  }

  def createCourseGoal(learningPathId: Long, courseId: Long): Long = {
    post(s"/learning-paths/$learningPathId/draft/goals/",
      s"""{
         |  "goalType": "course",
         |  "courseId": $courseId
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beOk

      (parse(body) \ "id").extract[Long]
    }
  }

  def createAssignmentGoalInGroup(groupId: Long, assignmentId: Long): Long = {
    post(s"/goal-groups/$groupId/goals/",
      s"""{
         |  "goalType": "assignment",
         |  "assignmentId": $assignmentId
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beOk

      (parse(body) \ "id").extract[Long]
    }
  }

  def createCourseGoalInGroup(groupId: Long, courseId: Long): Long = {
    post(s"/goal-groups/$groupId/goals/",
      s"""{
         |  "goalType": "course",
         |  "courseId": $courseId
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beOk

      (parse(body) \ "id").extract[Long]
    }
  }

  def createLessonGoalInGroup(groupId: Long, lessonId: Long): Long = {
    post(s"/goal-groups/$groupId/goals/",
      s"""{
         |  "goalType": "lesson",
         |  "lessonId": $lessonId
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beOk

      (parse(body) \ "id").extract[Long]
    }
  }

  def createActivityGoal(learningPathId: Long,
                         activityName: String = "testActivity",
                         count: Option[Int] = None,
                         optional: Boolean = false,
                         timeLimit: Option[Period] = None,
                         headers: Map[String, String] = Map()): Long = {
    post(s"/learning-paths/$learningPathId/draft/goals/",
      s"""{
         |  "goalType": "activity",
         |  "timeLimit": ${timeLimit.map(p => s""" "$p" """).getOrElse("null")} ,
         |  "activityName": "$activityName",
         |  "count": ${count.getOrElse("null")},
         |  "optional": $optional
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beOk

      (parse(body) \ "id").extract[Long]
    }
  }

  def createActivityInGroup(groupId: Long, activityName: String = "testActivity", count: Option[Int] = None): Long = {
    post(s"/goal-groups/$groupId/goals/",
      s"""{
         |  "goalType": "activity",
         |  "activityName": "$activityName",
         |  "count": ${count.getOrElse("null")}
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beOk

      (parse(body) \ "id").extract[Long]
    }
  }

  def createStatementGoal(lpId: Long,
                          verbId: String = "http://adlnet.gov/expapi/verbs/experienced",
                          objectId: String = "http://example.com/website",
                          objectName: String = "Example",
                          timeLimit: Option[Period] = None
                         ): Long = {
    post(s"/learning-paths/$lpId/draft/goals/",
      s"""{
         |  "goalType": "statement",
         |  "timeLimit": ${timeLimit.map(p => s""" "$p" """).getOrElse("null")} ,
         |  "verbId": "$verbId",
         |  "objectId": "$objectId",
         |  "objectName": "Example"
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beOk

      (parse(body) \ "id").extract[Long]
    }
  }

  def createStatementGoalInGroup(groupId: Long,
                                 verbId: String = "http://adlnet.gov/expapi/verbs/experienced",
                                 objectId: String = "http://example.com/website",
                                 objectName: String = "Example"): Long = {
    post(s"/goal-groups/$groupId/goals/",
      s"""{
         |  "goalType": "statement",
         |  "timeLimit": "P4D",
         |  "verbId": "$verbId",
         |  "objectId": "$objectId",
         |  "objectName": "Example"
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beOk

      (parse(body) \ "id").extract[Long]
    }
  }

  def createWebContentGoal(learningPathId: Long, webContentId: Long): Long = {
    post(s"/learning-paths/$learningPathId/draft/goals/",
      s"""{
         |  "goalType": "webContent",
         |  "webContentId": $webContentId
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beOk

      (parse(body) \ "id").extract[Long]
    }
  }

  def createWebContentGoalInGroup(groupId: Long, webContentId: Long): Long = {
    post(s"/goal-groups/$groupId/goals/",
      s"""{
         |  "goalType": "webContent",
         |  "webContentId": $webContentId
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beOk

      (parse(body) \ "id").extract[Long]
    }
  }

  def createTrainingEventGoal(learningPathId: Long, trainingEventId: Long): Long = {
    post(s"/learning-paths/$learningPathId/draft/goals/",
      s"""{
         |  "goalType": "trainingEvent",
         |  "trainingEventId": $trainingEventId
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beOk

      (parse(body) \ "id").extract[Long]
    }
  }

  def createTrainingEventInGroup(groupId: Long, trainingEventId: Long): Long = {
    post(s"/goal-groups/$groupId/goals/",
      s"""{
         |  "goalType": "trainingEvent",
         |  "trainingEventId": $trainingEventId
         |}""".stripMargin,
      jsonContentHeaders
    ) {
      status should beOk

      (parse(body) \ "id").extract[Long]
    }
  }

  def createGoalsTreeWithAllTypes(lpId: Long): Unit = {
    createLessonGoal(lpId, lessonId = 1)
    createActivityGoal(lpId)
    createStatementGoal(lpId)
    createAssignmentGoal(lpId, assignmentId = 2)
    createCourseGoal(lpId, courseId = 3)
    createTrainingEventGoal(lpId, trainingEventId = 4)
    createWebContentGoal(lpId, webContentId = 5)

    val groupId = createGoalGroup(lpId, s"test group")
    for {l1 <- 1 to 2} {
      createLessonGoalInGroup(groupId, lessonId = l1)
      createActivityInGroup(groupId, activityName = "a:" + l1)
      createStatementGoalInGroup(groupId, verbId = "v:" + l1, objectId = "o:" + l1)
      createAssignmentGoalInGroup(groupId, assignmentId = l1)
      createCourseGoalInGroup(groupId, courseId = l1)
      createTrainingEventInGroup(groupId, trainingEventId = l1)
      createWebContentGoalInGroup(groupId, webContentId = l1)

      val group2Id = createSubGoalGroup(groupId, s"g_${l1}_$l1")
      for {l2 <- 1 to 2} {
        createLessonGoalInGroup(group2Id, lessonId = l2)
        createActivityInGroup(group2Id, activityName = "a:" + l2)
        createStatementGoalInGroup(group2Id, verbId = "v:" + l2, objectId = "o:" + l2)
        createAssignmentGoalInGroup(group2Id, assignmentId = l2)
        createCourseGoalInGroup(groupId, courseId = l2)
        createTrainingEventInGroup(group2Id, trainingEventId = l2)
        createWebContentGoalInGroup(group2Id, webContentId = l2)
      }
    }
  }

  def addMember(lpId: Long, memberId: Long, memberType: MemberTypes.Value,
                headers: Map[String, String] = Map()): Unit = {
    post(s"/learning-paths/$lpId/members/${memberType}s",
      s"""[ $memberId ]""",
      jsonContentHeaders ++ headers
    ) {
      status should beNoContent
    }
  }

  def addMembers(lpId: Long, memberIds: Seq[Long],
                 memberType: MemberTypes.Value, headers: Map[String, String] = Map()): Unit = {
    post(s"/learning-paths/$lpId/members/${memberType}s/",
      s"""[ ${memberIds.mkString(", ")} ]""",
      jsonContentHeaders ++ headers
    ) {
      status should beNoContent
    }
  }

  def joinCurrentUser(lpId: Long, headers: Map[String, String]): Unit = {
    post(s"/learning-paths/$lpId/join",
      headers = headers
    ) {
      status should beNoContent
    }
  }

  def userCreatesNewLRActivity(servlet: ServletImpl, userId: Long, activityType: String)
                              (implicit companyId: Long): Unit = {
    servlet.lrActivityTypeService.addActivity(LRActivity(userId, activityType))
    await {
      new LRActivityListener(servlet.dbActions, servlet.taskManager)(servlet.executionContext)
        .onLRActivityCreated(userId, activityType)(companyId)
    }
  }

  def userSendsStatement(servlet: ServletImpl,
                         userId: Long,
                         verbId: String,
                         objectId: String,
                         timeStamp: DateTime = DateTime.now
                        )
                        (implicit companyId: Long): Unit = {
    await {
      servlet.statementListener
        .onStatementCreated(userId, StatementInfo(verbId, objectId, timeStamp))
    }
  }


  def getUserGoalStatuses(lpId: Long, userId: Long): Seq[UserGoalStatus] = {
    get(s"/learning-paths/$lpId/members/users/$userId/goals-progress/") {
      status should beOk
      parse(body).extract[Seq[UserGoalStatus]]
    }
  }

  def addCompetenceToLPDraft(lpId: Long, path: String, competence: String): Unit = {
    post(s"/learning-paths/$lpId/draft/$path", competence, jsonContentHeaders) {
      status should beNoContent
    }
  }

  def testCompetence(skillId: Long, levelId: Long): String = {
    s"""{
       |  "skillId": $skillId,
       |  "skillName": "Skill $skillId",
       |  "levelId": $levelId,
       |  "levelName": "Level $levelId"
       |} """.stripMargin
  }
}
