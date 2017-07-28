package com.arcusys.valamis.learningpath.migration

import com.arcusys.valamis.learningpath.migration.schema.old.OldCurriculumTables
import com.arcusys.valamis.learningpath.migration.schema.old.model.goal._
import com.arcusys.valamis.learningpath.migration.schema.old.model.{Certificate, CertificateMember, CertificateState, PeriodTypes, MemberTypes => OldMemberTypes}
import com.arcusys.valamis.learningpath.models.CertificateStatuses
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickProfile
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by pkornilov on 3/31/17.
  */
trait TestHelpers {
  self: SlickProfile with OldCurriculumTables =>

  import profile.api._

  def sampleCertificateGoal(goalType: GoalType.Value): CertificateGoal = {
    CertificateGoal(id = -1, certificateId = -1,
      goalType = goalType,
      periodType = PeriodTypes.DAYS, periodValue = 3,
      arrangementIndex = 3,
      isOptional = false,
      groupId = None, oldGroupId = None,
      modifiedDate = DateTime.now, userId = None, isDeleted = false
    )
  }

  def createSampleActiveCert(id: Long, title: String): DBIO[Int] = {
    val cert = Certificate(
      id = id,
      title = title,
      description = "Description of certificate " + id,
      isPublishBadge = true,
      shortDescription = "Badge description " + id,
      companyId = 20116L,
      validPeriodType = PeriodTypes.WEEKS,
      validPeriod = 7,
      createdAt = DateTime.now,
      activationDate = None,
      isActive = true,
      scope = None
    )
    createCertificate(cert)
  }


  def createSampleNotActiveCertificate(id: Long, title: String): DBIO[Int] = {
    val notActiveCert = Certificate(
      id = id,
      title = title,
      logo = "",
      isPermanent = true,
      description = "Description of certificate " + id,
      isPublishBadge = true,
      shortDescription = "Badge description " + id,
      companyId = 20116L,
      validPeriodType = PeriodTypes.WEEKS,
      validPeriod = 7,
      createdAt = DateTime.now,
      activationDate = None,
      isActive = false,
      scope = None
    )
    createCertificate(notActiveCert)
  }

  def addGoals(certId: Long, goals: Seq[(CertificateGoal, Option[Goal])]): DBIO[Seq[CertificateGoal]] = {
    DBIO.sequence(goals map { case (goal, goalData) =>
      for {
        goalId <- certificateGoals returning certificateGoals.map(_.id) += goal.copy(certificateId = certId)
        _ <- goalData.fold[DBIO[Int]](DBIO.successful(0)) {
          case g: PackageGoal => packageGoals += g.copy(goalId = goalId, certificateId = certId)
          case g: CourseGoal => courseGoals += g.copy(goalId = goalId, certificateId = certId)
          case g: AssignmentGoal => assignmentGoals += g.copy(goalId = goalId, certificateId = certId)
          case g: ActivityGoal => activityGoals += g.copy(goalId = goalId, certificateId = certId)
          case g: StatementGoal => statementGoals += g.copy(goalId = goalId, certificateId = certId)
          case g: TrainingEventGoal => trainingEventGoals += g.copy(goalId = goalId, certificateId = certId)
          case _ => DBIO.successful(0)
        }
      } yield goal.copy(id = goalId, certificateId = certId)
    })
  }

  def createCertificate(cert: Certificate): DBIO[Int] = {
    certificatesTQWithoutAutoInc += cert
  }

  def addUser(certId: Long, userId: Long): DBIO[Int] = {
    certificateMembers += CertificateMember(certId, userId, OldMemberTypes.User)
  }

  def addUserGoalState(goal: CertificateGoal, userId: Long, status: GoalStatuses.Value, now: DateTime): DBIO[Int] = {
    certificateGoalStates += CertificateGoalState(
      userId = userId,
      certificateId = goal.certificateId,
      goalId = goal.id,
      status = status,
      modifiedDate = now,
      isOptional = goal.isOptional
    )
  }

  def addUserLPState(certId: Long, userId: Long, status: CertificateStatuses.Value, now: DateTime): DBIO[Int] = {
    certificateStates += CertificateState(
      userId = userId,
      status = status,
      statusAcquiredDate = now,
      userJoinedDate = now,
      certificateId = certId

    )
  }

  def getNowWithoutMillis(): DateTime = {
    val current = DateTime.now
    current.minusMillis(current.getMillisOfSecond)
  }
}