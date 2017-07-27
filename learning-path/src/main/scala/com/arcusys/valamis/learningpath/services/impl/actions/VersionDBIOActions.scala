package com.arcusys.valamis.learningpath.services.impl.actions

import com.arcusys.valamis.learningpath.models._
import com.arcusys.valamis.learningpath.services.impl.tables.LearningPathTables
import com.arcusys.valamis.learningpath.services.impl.tables.utils.SlickProfile
import org.joda.time.DateTime
import slick.driver.JdbcProfile

/**
  * Created by mminin on 26/01/2017.
  */
class VersionDBIOActions(val profile: JdbcProfile)
  extends LearningPathTables
    with SlickProfile {

  import profile.api._

  private val selectByIdQ = Compiled { id: Rep[Long] =>
    versionTQ.filter(_.id === id)
  }

  private val selectPublishedByIdQ = Compiled { id: Rep[Long] =>
    versionTQ.filter(_.id === id).map(_.published)
  }

  private val selectByLearningPathIdOrderByAgeQ = Compiled { learningPathId: Rep[Long] =>
    versionTQ
      .filter(_.learningPathId === learningPathId)
      .sortBy(_.createdDate.desc)
  }

  private val selectLearningsPathWithValidPeriodQ = Compiled { (companyId: Rep[Long],
                                                                endDate: Rep[DateTime]) =>

    val certificateStatuses = Seq(CertificateStatuses.InProgress, CertificateStatuses.Success)
    versionTQ.filterNot(_.validPeriod.isEmpty)
      .join(learningPathTQ).on((v, lp) => lp.id === v.learningPathId && lp.currentVersionId === v.id)
      .filter { case (_, lp) => lp.companyId === companyId }
      .map { case (v, lp) => v }
      .join(userLPStatusTQ).on((v, u) => v.learningPathId === u.learningPathId)
      .filter(_._2.status.inSet(certificateStatuses))
      .filter(_._2.startedDate <= endDate)
  }

  private val selectCurrentByLearningPathIdQ = Compiled { learningPathId: Rep[Long] =>
    learningPathTQ
      .filter(_.id === learningPathId)
      .join(versionTQ).on((lp,v) => lp.id === v.learningPathId && lp.currentVersionId === v.id)
      .map{ case (lp,v) => (v.id, v.properties)}
  }

  private val selectUnpublishedByLearningPathIdOrderByAgeQ = Compiled { learningPathId: Rep[Long] =>
    versionTQ
      .filter(v => v.learningPathId === learningPathId && !v.published)
      .sortBy(_.createdDate.desc)
  }

  private val selectWithLearningPathByCourseIdQ = Compiled { courseId: Rep[Long] =>
    learningPathTQ
      .join(versionTQ).on((lp, v) => v.id === lp.currentVersionId)
      .filter { case (lp, v) => v.courseId === courseId }
      .map { case (lp, v) => (lp, v.properties) }
  }

  private val selectWithLearningPathByCompanyIdQ = Compiled { companyId: Rep[Long] =>
    learningPathTQ
      .join(versionTQ).on((lp, v) => v.id === lp.currentVersionId)
      .filter { case (lp, v) => lp.companyId === companyId }
      .map { case (lp, v) => (lp, v.properties) }
  }

  private val selectWithLearningPathByIdQ = Compiled { versionId: Rep[Long] =>
    learningPathTQ
      .join(versionTQ).on((lp, v) => v.id === lp.currentVersionId)
      .filter { case (lp, v) => v.id === versionId }
      .map { case (lp, v) => (lp, v.properties) }
  }

  private def selectWithLearningPathByIdsQ(ids: Seq[Long]) =
    learningPathTQ
      .join(versionTQ).on((lp, v) => v.id === lp.currentVersionId)
      .filter { case (lp, v) => lp.id inSet ids }
      .map { case (lp, v) => (lp, v.properties) }

  private val selectDataFieldsQ = Compiled {
    versionTQ.map(e => (e.learningPathId, e.title, e.description, e.logo,
      e.courseId, e.validPeriod, e.expiringPeriod, e.openBadgesEnabled, e.openBadgesDescription,
      e.published, e.createdDate, e.modifiedDate)
    )
  }

  private val selectUpdatableFieldsQ = Compiled { id: Rep[Long] =>
    versionTQ
      .filter(_.id === id)
      .map(e => (e.title, e.description, e.validPeriod,
        e.expiringPeriod, e.openBadgesEnabled, e.openBadgesDescription,
        e.courseId, e.modifiedDate)
      )
  }

  private val selectModifiedDateQ = Compiled { id: Rep[Long] =>
    versionTQ
      .filter(_.id === id)
      .map(_.modifiedDate)
  }

  private val selectPublishedQ = Compiled { id: Rep[Long] =>
    versionTQ
      .filter(_.id === id)
      .map(_.published)
  }

  private val selectLogoAndModifiedDateQ = Compiled { id: Rep[Long] =>
    versionTQ
      .filter(_.id === id)
      .map(x => (x.logo, x.modifiedDate))
  }

  private val selectCountByLogoQ = Compiled { logo: Rep[String] =>
    versionTQ
      .filter(_.logo === logo.?)
      .length
  }

  def insert(version: LPVersion): DBIO[Long] = {
    selectDataFieldsQ returning versionTQ.map(_.id) += LPVersion.unapply(version).get
  }

  def update(id: Long, e: LPProperties, modifiedDate: DateTime): DBIO[Int] = {
    selectUpdatableFieldsQ(id) update {
      (e.title, e.description, e.validPeriod, e.expiringPeriod,
        e.openBadgesEnabled, e.openBadgesDescription, e.courseId, modifiedDate)
    }
  }

  def updateModifiedDate(id: Long, modifiedDate: DateTime): DBIO[Int] = {
    selectModifiedDateQ(id) update modifiedDate
  }

  def updatePublished(id: Long, published: Boolean): DBIO[Int] = {
    selectPublishedQ(id) update published
  }

  def updateLogoAndModifiedDate(id: Long, logo: Option[String], modifiedDate: DateTime): DBIO[Int] = {
    selectLogoAndModifiedDateQ(id) update (logo, modifiedDate)
  }


  def getById(id: Long): DBIO[Option[(Long, LPVersion)]] = {
    selectByIdQ(id).result.headOption
  }

  def isPublishedById(id: Long): DBIO[Option[Boolean]] = {
    selectPublishedByIdQ(id).result.headOption
  }

  def getCurrentByLearningPathId(learningPathId: Long): DBIO[Option[(Long, LPVersion)]] = {
    selectCurrentByLearningPathIdQ(learningPathId).result.headOption
  }

  def getLastByLearningPathId(learningPathId: Long): DBIO[Option[(Long, LPVersion)]] = {
    selectByLearningPathIdOrderByAgeQ(learningPathId).result.headOption
  }

  def getDraftByLearningPathId(learningPathId: Long): DBIO[Option[(Long, LPVersion)]] = {
    selectUnpublishedByLearningPathIdOrderByAgeQ(learningPathId).result.headOption
  }

  def getByLearningPathId(learningPathId: Long): DBIO[Seq[(Long, LPVersion)]] = {
    selectByLearningPathIdOrderByAgeQ(learningPathId).result
  }

  def getLearningPathWithValidPeriod(endDate: DateTime)
                                    (implicit companyId: Long): DBIO[Seq[((Long, LPVersion), UserLPStatus)]] = {
    selectLearningsPathWithValidPeriodQ(companyId, endDate).result

  }

  def getAllLearningPathWithValidPeriod: DBIO[Seq[((Long, LPVersion),
    LearningPath, UserLPStatus)]] = {
    val certificateStatuses = Seq(CertificateStatuses.InProgress, CertificateStatuses.Success)
    versionTQ.filterNot(_.validPeriod.isEmpty)
      .join(learningPathTQ).on((v, lp) => lp.id === v.learningPathId &&
      lp.currentVersionId === v.id)
      .join(userLPStatusTQ).on((v, u) => v._1.learningPathId === u.learningPathId)
      .filter(_._2.status.inSet(certificateStatuses))
      .map { data => (data._1._1, data._1._2, data._2) }
      .result
  }

  def deleteByLearningPathId(learningPathId: Long): DBIO[Int] = {
    selectByLearningPathIdOrderByAgeQ(learningPathId).delete
  }

  def getWithLearningPathByCourseId(courseId: Long): DBIO[Seq[(LearningPath, LPVersion)]] = {
    selectWithLearningPathByCourseIdQ(courseId).result
  }

  def getWithLearningPathByCompanyId(companyId: Long): DBIO[Seq[(LearningPath, LPVersion)]] = {
    selectWithLearningPathByCompanyIdQ(companyId).result
  }

  def getWithLearningPathById(versionId: Long): DBIO[Option[(LearningPath, LPVersion)]] = {
    selectWithLearningPathByIdQ(versionId).result.headOption
  }

  def getWithLearningPathByIds(ids: Seq[Long]): DBIO[Seq[(LearningPath, LPVersion)]] = {
    selectWithLearningPathByIdsQ(ids).result
  }

  def getCountByLogo(logo: String): DBIO[Int] = {
    selectCountByLogoQ(logo).result
  }
}
