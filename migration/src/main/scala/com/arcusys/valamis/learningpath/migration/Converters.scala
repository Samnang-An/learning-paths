package com.arcusys.valamis.learningpath.migration

import com.arcusys.valamis.learningpath.migration.schema.old.model.goal.{CertificateGoalState, GoalType, GoalStatuses => OldGoalStatuses}
import com.arcusys.valamis.learningpath.migration.schema.old.model.{Certificate, CertificateMember, CertificateState, MemberTypes => OldMemberTypes}
import com.arcusys.valamis.learningpath.models._
import com.arcusys.valamis.members.picker.model.{Member, MemberTypes}
import org.joda.time.{DateTime, Period}

/**
  * Created by pkornilov on 3/24/17.
  */
trait Converters {

  def isMemberExisted(memberId: Long, memberType: MemberTypes.Value): Boolean

  private[migration] def toLPMembers(lpId: Long, oldMembers: Seq[CertificateMember]) = {
    oldMembers collect {
      case old if isMemberExisted(old.memberId, toNewMemberType(old.memberType)) =>
        Member(
          id = old.memberId,
          tpe = toNewMemberType(old.memberType),
          entityId = lpId
        )
    }
  }

  private[migration] def toUserGoalStatuses(oldGoalIdToNewGoal: Map[Long, Goal],
                                            certStates: Seq[CertificateState],
                                            goalStates: Seq[CertificateGoalState])
                                           (implicit oldCertificate: Certificate) = {


    val certStatesByUserId = certStates groupBy (_.userId) mapValues (_.head)

    goalStates collect {
      case old if oldGoalIdToNewGoal.get(old.goalId).isDefined =>
        val newStartDate =
          getGoalStartDate(oldCertificate, certStatesByUserId.get(old.userId)).getOrElse(old.modifiedDate)
        val goal = oldGoalIdToNewGoal(old.goalId)

        val completedCount = if (old.status == OldGoalStatuses.Success) 1 else 0

        UserGoalStatus(
          userId = old.userId,
          goalId = goal.id,
          status = toNewGoalStatus(old.status),
          startedDate = newStartDate,
          modifiedDate = old.modifiedDate,
          requiredCount = 1,
          completedCount = completedCount,
          endDate = goal.timeLimit.map(newStartDate plus _)
        )
    }
  }

  private def getGoalStartDate(cert: Certificate, state: Option[CertificateState]): Option[DateTime] = {
    val dates = state.map(_.userJoinedDate).toSeq ++ cert.activationDate.toSeq
    dates match {
      case Nil => None
      case xs => Some(xs.max)
    }

  }

  private[migration] def toUserLPStatuses(lpId: Long,
                                          versionId: Long,
                                          validPeriod: Option[Period],
                                          oldStates: Seq[CertificateState]) = {
    oldStates map { old =>

      val (newStatus, modifiedDate) =
        if (old.status == CertificateStatuses.Overdue) {
          //there is no overdue status in LP, so we convert it to Success
          (CertificateStatuses.Success, old.statusAcquiredDate.minus(validPeriod.getOrElse(Period.ZERO)))
        } else {
          (old.status, old.statusAcquiredDate)
        }

      val progress = if (newStatus == CertificateStatuses.Success) 1 else 0 //will be filled later during migration}

      UserLPStatus(
        userId = old.userId,
        learningPathId = lpId,
        versionId = versionId,
        status = newStatus,
        startedDate = old.userJoinedDate,
        modifiedDate = modifiedDate,
        progress
      )
    }
  }

  private[migration] def toNewGoalStatus(old: OldGoalStatuses.Value): GoalStatuses.Value = {
    old match {
      case OldGoalStatuses.InProgress => GoalStatuses.InProgress
      case OldGoalStatuses.Failed => GoalStatuses.Failed
      case OldGoalStatuses.Success => GoalStatuses.Success
    }
  }

  private[migration] def toNewGoalType(oldGoalType: GoalType.Value): GoalTypes.Value = {
    oldGoalType match {
      case GoalType.Activity => GoalTypes.LRActivity
      case GoalType.Assignment => GoalTypes.Assignment
      case GoalType.Course => GoalTypes.Course
      case GoalType.Package => GoalTypes.Lesson
      case GoalType.Statement => GoalTypes.Statement
      case GoalType.TrainingEvent => GoalTypes.TrainingEvent
    }
  }

  private[migration] def toNewMemberType(old: OldMemberTypes.Value): MemberTypes.Value = {
    old match {
      case OldMemberTypes.Organization => MemberTypes.Organization
      case OldMemberTypes.Role => MemberTypes.Role
      case OldMemberTypes.UserGroup => MemberTypes.UserGroup
      case OldMemberTypes.User => MemberTypes.User
    }
  }

  implicit val dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isBefore _)

}