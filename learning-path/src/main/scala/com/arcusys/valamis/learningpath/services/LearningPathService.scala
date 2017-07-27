package com.arcusys.valamis.learningpath.services

import com.arcusys.valamis.learningpath.models._
import com.arcusys.valamis.members.picker.model.SkipTake

import scala.concurrent.Future

/**
  * Created by mminin on 20/01/2017.
  */
trait LearningPathService {

  def create(userId: Long,
             properties: LPProperties)
            (implicit companyId: Long): Future[(LearningPath, LPVersion)]

  def getById(id: Long)
             (implicit companyId: Long): Future[Option[(LearningPath, LPVersion)]]

  def getByIds(ids: Seq[Long]): Future[Seq[(LearningPath, LPVersion)]]

  def getWithUserStatusByIds(userId: Long,
                             ids: Seq[Long]): Future[Seq[LPWithVersionAndStatus]]

  def getByTitleWithSucceededUserIds(title: String,
                                     count: Option[Int] = None)
                                    (implicit companyId: Long): Future[Seq[LPWithVersionAndSucceededUserIds]]

  def getByUserIdsWithSucceededUserCount(userIds: Seq[Long], skipTake: Option[SkipTake] = None)
                                        (implicit companyId: Long): Future[Seq[LPWithVersionAndSucceededUserCount]]

  def delete(id: Long)
            (implicit companyId: Long): Future[Unit]

  def getDraftById(id: Long)
                  (implicit companyId: Long): Future[Option[(LearningPath, LPVersion)]]

  def getByFilter(filter: LearningPathFilter,
                  sort: LearningPathSort.Value,
                  skip: Option[Int],
                  take: Int)
                 (implicit companyId: Long): Future[Seq[(LearningPath, LPVersion)]]

  def getCountByFilter(filter: LearningPathFilter)
                      (implicit companyId: Long): Future[Int]

  /**
    * search learning paths and return with additional info
    *
    * @param userMemberId user id to get progress
    * @param joined       is user joined filter: yes, no or do not matter
    */
  def getByFilterForMember(filter: LearningPathFilter,
                           userMemberId: Long,
                           joined: Option[Boolean],
                           statusFilter: Option[CertificateStatuses.Value],
                           sort: LearningPathSort.Value,
                           skip: Option[Int],
                           take: Option[Int])
                          (implicit companyId: Long): Future[Seq[LPWithInfo]]

  def getByIdForMember(learningPathId: Long,
                       userMemberId: Long)
                      (implicit companyId: Long): Future[Option[LPWithInfo]]

  def getCountByFilterForMember(filter: LearningPathFilter,
                                userMemberId: Long,
                                joined: Option[Boolean],
                                statusFilter: Option[CertificateStatuses.Value])
                               (implicit companyId: Long): Future[Int]


  def updateDraft(id: Long, properties: LPProperties)
                 (implicit companyId: Long): Future[(LearningPath, LPVersion)]

  def publishDraft(id: Long)
                  (implicit companyId: Long): Future[Unit]

  def deactivate(id: Long)
                (implicit companyId: Long): Future[Unit]

  def activate(id: Long)
              (implicit companyId: Long): Future[Unit]

  def clone(id: Long)
           (implicit companyId: Long): Future[(LearningPath, LPVersion)]
}
