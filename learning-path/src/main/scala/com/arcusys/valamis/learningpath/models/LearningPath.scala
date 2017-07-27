package com.arcusys.valamis.learningpath.models

import com.arcusys.valamis.members.picker.model.IdAndName
import org.joda.time.{DateTime, Period}

/**
  * Learning path
  *
  * @param currentVersionId link to last published version, or draft if no published version
  */
case class LearningPath(id: Long,
                        activated: Boolean,
                        companyId: Long,
                        userId: Long,
                        hasDraft: Boolean,
                        currentVersionId: Option[Long])

case class LPProperties(title: String,
                        description: Option[String],

                        courseId: Option[Long],
                        validPeriod: Option[Period],

                        expiringPeriod: Option[Period],

                        openBadgesEnabled: Boolean = false,
                        openBadgesDescription: Option[String])

case class LPWithInfo(learningPath: LearningPath,
                      versionProperties: LPVersion,
                      goalsCount: Int,
                      userMembersCount: Int,
                      userStatus: Option[UserLPStatus])

/**
  * Used in Competences portlet
  */

case class LPWithSucceededUsers(id: Long,
                                title: String,
                                shortDescription: String,
                                description: String,
                                logo: String,
                                succeededUsers: Seq[IdAndName])

case class LPWithSucceededUserCount(id: Long,
                                title: String,
                                shortDescription: String,
                                description: String,
                                logo: String,
                                succeededUserCount: Long)


case class LPVersion(learningPathId: Long,
                     title: String,
                     description: Option[String],
                     logo: Option[String],

                     courseId: Option[Long],
                     validPeriod: Option[Period],
                     expiringPeriod: Option[Period],

                     openBadgesEnabled: Boolean,
                     openBadgesDescription: Option[String],

                     published: Boolean,
                     createdDate: DateTime,
                     modifiedDate: DateTime)

case class LearningPathWithVersion(learningPath: LearningPath,
                                   version: LPVersion)

case class LearningPathFilter(title: Option[String],
                              courseId: Option[Long],
                              userId: Option[Long],
                              published: Option[Boolean],
                              activated: Option[Boolean])

object LearningPathSort extends Enumeration {
  val title = Value("title")
  val titleDesc = Value("-title")
  val creationDate = Value("createdDate")
  val creationDateDesc = Value("-createdDate")
}


case class PathUser(learningPathId: Long,
                    userId: Long,
                    startDate: DateTime)

case class GoalUser(goalId: Long,
                    userId: Long,
                    startDate: DateTime,
                    status: GoalStatuses.Value)

trait CustomGoal {
  def title: String

  def goalType: String
}


object LPMessageActions {
  val IsDeployed = "idDeployed"
  val UsersToLPCount = "usersToLPCount"
  val GetLPById = "getLPById"
  val GetLPByIds = "getLPByIds"
  val GetLPWithStatusByIds = "getLPWithStatusByIds"
  val GetPassedLP = "getPassedLP"
}

object LPMessageFields {
  val Action = "action"
  val StartDate = "startDate"
  val EndDate = "endDate"
  val CompanyId = "companyId"
  val Id = "id"
  val UserId = "userId"
  val Ids = "ids"
}