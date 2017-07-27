package com.arcusys.valamis.learningpath.services.impl.actions

import com.arcusys.valamis.learningpath.models._
import com.arcusys.valamis.learningpath.services.{LPWithVersionAndSucceededUserCount, LPWithVersionAndSucceededUserIds}
import com.arcusys.valamis.learningpath.services.impl.tables.LearningPathTables
import com.arcusys.valamis.learningpath.services.impl.tables.utils.{FilterUtils, SlickProfile}
import com.arcusys.valamis.members.picker.model.SkipTake
import org.joda.time.DateTime
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

/**
  * Created by mminin on 26/01/2017.
  */
class LearningPathDBIOActions(val profile: JdbcProfile)
                             (implicit val executionContext: ExecutionContext)
  extends LearningPathTables
    with SlickProfile
    with FilterUtils {

  import profile.api._

  private val selectByIdQ = Compiled { (id: Rep[Long], companyId: Rep[Long]) =>
    learningPathTQ
      .filter(lp => lp.id === id && lp.companyId === companyId)
  }

  private val selectByCompanyIdQ = Compiled { companyId: Rep[Long] =>
    learningPathTQ.filter(_.companyId === companyId)
  }

  private val selectValuableFieldsQ = Compiled {
    learningPathTQ.map(e => (e.companyId, e.userId, e.activated, e.hasDraft))
  }

  private val selectCurrentVersionIdByIdQ = Compiled { id: Rep[Long] =>
    learningPathTQ
      .filter(_.id === id)
      .map(_.currentVersionId)
  }

  private val selectHasDraftByIdQ = Compiled { id: Rep[Long] =>
    learningPathTQ
      .filter(_.id === id)
      .map(_.hasDraft)
  }

  private val selectActivatedByIdQ = Compiled { id: Rep[Long] =>
    learningPathTQ
      .filter(_.id === id)
      .map(_.activated)
  }

  private val selectActiveCurrentVersionAndHasDraftByIdQ = Compiled { id: Rep[Long] =>
    learningPathTQ
      .filter(_.id === id)
      .map(x => (x.activated, x.currentVersionId, x.hasDraft))
  }

  private val selectActiveQ = Compiled { _: Unit =>
    learningPathTQ.filter(lp => lp.activated && lp.currentVersionId.isDefined)
  }

  private def buildQueryToSelectWithSucceededUserIds(companyId: Rep[Long])
                                                    (rowFilter: (LearningPathTable, LPVersionTable, UserStatusTable)
                                                      => Rep[Boolean]) = {
    learningPathTQ join versionTQ on {
      case (lp, v) => lp.currentVersionId === v.id
    } join userLPStatusTQ on {
      case ((lp, _), status) => status.learningPathId === lp.id
    } filter {
      case ((lp, v), status) =>
        lp.companyId === companyId && lp.activated && status.status === CertificateStatuses.Success &&
          rowFilter(lp, v, status)
    } map {
      case ((lp, v), status) => (lp, v.properties, status)
    }
  }

  private val selectByTitleWithSucceededUsersQ = Compiled {
    (companyId: Rep[Long], titleLikePattern: Rep[String]) =>
      buildQueryToSelectWithSucceededUserIds(companyId) { (_, v, _) =>
        v.title.toLowerCase.like(titleLikePattern)
      }
  }

  def getByTitleWithSucceededUserIds(title: String,
                                     count: Option[Int] = None)
                                    (implicit companyId: Long): DBIO[Seq[LPWithVersionAndSucceededUserIds]] = {
    val titleLikePattern = "%" + title.toLowerCase + "%"
    selectByTitleWithSucceededUsersQ(companyId, titleLikePattern).result map { rows =>
      val results = rows groupBy {
        case (lp, _, _) => lp.id
      } map {
        case (_, items) =>
          val (lp, v, _) = items.head
          val userIds = items map {
            case (_, _, status) => status.userId
          }
          (lp, v, userIds)
      } toSeq

      val sorted = results sortBy {
        case (_, v, _) => v.title
      }

      count.fold(sorted)(sorted.take)
    }
  }

  def getByUserIdsWithSucceededUserIds(userIds: Seq[Long], skipTake: Option[SkipTake] = None)
                             (implicit companyId: Long): DBIO[Seq[LPWithVersionAndSucceededUserCount]] = {
    withFilterByIds(userIds) { ids =>
      buildQueryToSelectWithSucceededUserIds(companyId) {
        (_, _, userStatus) => userStatus.userId inSet ids
      }.result
    } map { rows =>
      val results = rows groupBy {
        case (lp, _, _) => lp.id
      } map {
        case (_, items) =>
          val (lp, v, _) = items.head
          val userCount = items.size
          (lp, v, userCount)
      } toSeq

      val ordering: Ordering[DateTime] = Ordering.fromLessThan(_ isBefore _)

      val sorted = results.sortBy {
        case (_, v, _) => v.modifiedDate
      }(ordering.reverse)

      skipTake match {
        case None => sorted
        case Some(SkipTake(s, t)) => sorted.slice(s, s + t)
      }

    }
  }


  def insert(companyId: Long, userId: Long, active: Boolean, hasDraft: Boolean): DBIO[Long] = {
    selectValuableFieldsQ returning learningPathTQ.map(_.id) += (companyId, userId, active, hasDraft)
  }

  def getById(id: Long)
             (implicit companyId: Long): DBIO[Option[LearningPath]] = {
    selectByIdQ(id, companyId).result.headOption
  }

  private val selectWithCurrentVersionByIdQ = Compiled { (id: Rep[Long], companyId: Rep[Long]) =>
    learningPathTQ
      .join(versionTQ).on((lp, v) => lp.currentVersionId === v.id)
      .filter { case (lp, v) => lp.id === id && lp.companyId === companyId }
      .map { case (lp, v) => (lp, v.properties) }
  }


  def getWithCurrentVersionById(id: Long)
                               (implicit companyId: Long): DBIO[Option[(LearningPath, LPVersion)]] = {
    selectWithCurrentVersionByIdQ(id, companyId).result.headOption
  }

  def deleteById(id: Long)
                (implicit companyId: Long): DBIO[Int] = {
    selectByIdQ(id, companyId).delete
  }

  def getByCompanyIdQ(companyId: Long): DBIO[Seq[LearningPath]] = {
    selectByCompanyIdQ(companyId).result
  }

  def updateCurrentVersionId(id: Long, versionId: Long): DBIO[Int] = {
    selectCurrentVersionIdByIdQ(id) update Some(versionId)
  }

  def updateActiveCurrentVersionAndHasDraft(id: Long,
                                            active: Boolean,
                                            currentVersionId: Long,
                                            hasDraft: Boolean): DBIO[Int] = {
    selectActiveCurrentVersionAndHasDraftByIdQ(id) update(active, Some(currentVersionId), hasDraft)
  }

  def selectAllActive: DBIO[Seq[LearningPath]] =
    selectActiveQ({}).result

  def updateHasDraft(id: Long,
                     hasDraft: Boolean): DBIO[Int] = {
    selectHasDraftByIdQ(id) update hasDraft
  }

  def updateActivated(id: Long, activated: Boolean): DBIO[Int] = {
    selectActivatedByIdQ(id) update activated
  }

  //TODO: make query compilable
  private def getQueryByFilter(filter: LearningPathFilter)
                              (implicit companyId: Long) = {
    var query = learningPathTQ
      .join(versionTQ).on((lp, v) => v.id === lp.currentVersionId)
      .filter { case (lp, v) => lp.companyId === companyId }

    for (titleFilter <- filter.title)
      query = query.filter {
        case (lp, v) => v.title.toLowerCase.like("%" + titleFilter.toLowerCase + "%")
      }

    for (courseIdFilter <- filter.courseId)
      query = query.filter { case (lp, v) => v.courseId === courseIdFilter }

    for (userIdFilter <- filter.userId)
      query = query.filter { case (lp, v) => lp.userId === userIdFilter }

    for (published <- filter.published)
      query = query.filter { case (lp, v) => v.published === published }

    for (activated <- filter.activated)
      query = query.filter { case (lp, v) => lp.activated === activated }

    query
  }

  def getByFilter(filter: LearningPathFilter,
                  sort: LearningPathSort.Value,
                  skip: Int,
                  take: Int)
                 (implicit companyId: Long): DBIO[Seq[(LearningPath, LPVersion)]] = {

    var query = getQueryByFilter(filter)

    query = sort match {
      case LearningPathSort.title => query.sortBy { case (lp, v) => v.title }
      case LearningPathSort.titleDesc => query.sortBy { case (lp, v) => v.title.desc }
      case LearningPathSort.creationDate => query.sortBy { case (lp, v) => v.createdDate }
      case LearningPathSort.creationDateDesc => query.sortBy { case (lp, v) => v.createdDate.desc }
    }

    query
      .map { case (lp, v) => (lp, v.properties) }
      .drop(skip).take(take)
      .result
  }


  def getCountByFilter(filter: LearningPathFilter)
                      (implicit companyId: Long): DBIO[Int] = {
    val query = getQueryByFilter(filter)

    query.length.result
  }


  private def getQueryByMember(filter: LearningPathFilter,
                               userMemberId: Long,
                               joined: Option[Boolean])
                              (implicit companyId: Long) = {
    val query = getQueryByFilter(filter)

    val learningPathIdWithMemberQ = usersMembershipTQ
      .filter(_.userId === userMemberId)
      .groupBy(_.entityId).map(_._1)

    joined match {
      case None => query
      case Some(true) => query.filter { case (lp, v) => lp.id in learningPathIdWithMemberQ }
      case Some(false) => query.filterNot { case (lp, v) => lp.id in learningPathIdWithMemberQ }
    }
  }

  def getCountByFilterForMember(filter: LearningPathFilter,
                                userMemberId: Long,
                                joined: Option[Boolean],
                                statusFilter: Option[CertificateStatuses.Value])
                               (implicit companyId: Long): DBIO[Int] = {
    var query = getQueryByMember(filter, userMemberId, joined)

    for (status <- statusFilter) {
      val userStatusesQ = userLPStatusTQ.filter(_.userId === userMemberId)
      query = query
        .join(userStatusesQ)
        .on { case (row, userStatus) => row._1.id === userStatus.learningPathId }
        .filter { case (_, userStatus) => userStatus.status === status }
        .map { case ((lp, v), _) => (lp, v) }
    }

    query.length.result
  }

  def getFullInfoByFilter(filter: LearningPathFilter,
                          userMemberId: Long,
                          joined: Option[Boolean],
                          statusFilter: Option[CertificateStatuses.Value],
                          sort: LearningPathSort.Value,
                          skip: Int,
                          take: Option[Int])
                         (implicit companyId: Long): DBIO[Seq[LPWithInfo]] = {
    val query = getQueryByMember(filter, userMemberId, joined)

    var queryWithData = joinAdditionalData(query, userMemberId)

    for (status <- statusFilter) {
      queryWithData = queryWithData
        .filter { case (_, _, _, _, (userStatus, _, _, _, _)) => userStatus === status }
    }

    queryWithData = sort match {
      case LearningPathSort.title => queryWithData.sortBy(_._2.title)
      case LearningPathSort.titleDesc => queryWithData.sortBy(_._2.title.desc)
      case LearningPathSort.creationDate => queryWithData.sortBy(_._2.createdDate)
      case LearningPathSort.creationDateDesc => queryWithData.sortBy(_._2.createdDate.desc)
    }

    queryWithData = queryWithData.drop(skip)

    for (t <- take) {
      queryWithData = queryWithData.take(t)
    }

    queryWithData
      .result
      .map(_.map {
        case (lp, (v, properties), goalsCount, usersCount, statusData) =>
          val userStatus = statusData match {
            case (Some(status), Some(startedDate), Some(modifiedDate), Some(progress), Some(versionId)) =>
              Some(UserLPStatus(userMemberId, lp.id, versionId, status, startedDate, modifiedDate, progress))
            case _ => None
          }
          LPWithInfo(lp, properties, goalsCount, usersCount, userStatus)
      })
  }

  def getFullInfoById(learningPathId: Long,
                      userMemberId: Long)
                     (implicit companyId: Long): DBIO[Option[LPWithInfo]] = {
    val query = learningPathTQ
      .join(versionTQ).on((lp, v) => v.id === lp.currentVersionId)
      .filter { case (lp, v) => lp.companyId === companyId && lp.id === learningPathId }

    joinAdditionalData(query, userMemberId)
      .result
      .headOption
      .map(_.map {
        case (lp, (v, properties), goalsCount, usersCount, statusData) =>
          val userStatus = statusData match {
            case (Some(status), Some(startedDate), Some(modifiedDate), Some(progress), Some(versionId)) =>
              Some(UserLPStatus(userMemberId, lp.id, versionId, status, startedDate, modifiedDate, progress))
            case _ => None
          }
          LPWithInfo(lp, properties, goalsCount, usersCount, userStatus)
      })
  }

  private def joinAdditionalData(query: Query[(LearningPathTable, LPVersionTable), (LearningPath, (Long, LPVersion)), Seq],
                                 userMemberId: Long) = {
    val versionIdToGoalsCountQ = goalTQ
      .groupBy(_.versionId)
      .map { case (versionId, goals) => (versionId, goals.length) }

    val learningPathIdToUsersCountQ = usersMembershipTQ
      .groupBy(_.entityId)
      .map { case (lpId, userMembers) => (lpId, userMembers.map(_.userId).countDistinct) }

    val userStatusesQ = userLPStatusTQ.filter(_.userId === userMemberId)

    query
      .joinLeft(versionIdToGoalsCountQ)
      .on { case (row, (versionId, _)) => row._2.id === versionId }
      .map { case ((lp, v), goalsCount) => (lp, v, goalsCount.map(_._2).getOrElse(0)) }

      .joinLeft(learningPathIdToUsersCountQ)
      .on { case (row, (learningPathId, _)) => row._1.id === learningPathId }
      .map { case ((lp, v, goalsCount), usersCount) =>
        (lp, v, goalsCount, usersCount.map(_._2).getOrElse(0))
      }

      .joinLeft(userStatusesQ)
      .on { case (row, status) => row._1.id === status.learningPathId }
      .map { case ((lp, v, goalsCount, usersCount), status) =>
        //status is optional, we cant use it as it is ( will fail on MySql and Oracle )
        (lp, v, goalsCount, usersCount,
          (status.map(_.status),
            status.map(_.startedDate), status.map(_.modifiedDate),
            status.map(_.progress), status.map(_.versionId)
          )
        )
      }
  }
}
