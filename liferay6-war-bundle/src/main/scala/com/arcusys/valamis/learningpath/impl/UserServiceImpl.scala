package com.arcusys.valamis.learningpath.impl

import com.arcusys.valamis.learningpath.models.patternreport.SkipTake
import com.arcusys.valamis.learningpath.services.UserService
import com.arcusys.valamis.lrs.tincan.{Account, Agent}
import com.liferay.portal.kernel.dao.orm.QueryUtil
import com.liferay.portal.kernel.search.{Field, Sort}
import com.liferay.portal.kernel.util.GetterUtil
import com.liferay.portal.kernel.workflow.WorkflowConstants
import com.liferay.portal.service.UserLocalServiceUtil

import scala.language.postfixOps
import scala.collection.JavaConverters._

/**
  * Created by mminin on 22/03/2017.
  */
class UserServiceImpl extends UserService {

  val ALL = QueryUtil.ALL_POS
  val sort = new Sort("firstName", Sort.STRING_TYPE, false)

  override def getUserName(userId: Long): String = {
    UserLocalServiceUtil.getUser(userId).getFullName
  }

  override def getUserUUID(userId: Long): String = {
    UserLocalServiceUtil.getUser(userId).getUuid
  }

  override def search(keyword: Option[String],
                      skipTake: Option[SkipTake])
                     (implicit companyId: Long): Seq[Long] = {

    val (start, end) = skipTake map {
      st => (st.skip, st.skip + st.take)
    } getOrElse (ALL, ALL)

    val hits = UserLocalServiceUtil
      .search(companyId,
        keyword.orNull,
        WorkflowConstants.STATUS_APPROVED,
        null,
        start,
        end,
        sort)

    hits.toList.asScala map {
      document => GetterUtil.getLong(document.get(Field.USER_ID))
    }
  }

  override def getAgent(userId: Long, companyId: Long, homePage: String): Agent = {
    val user = UserLocalServiceUtil.getUser(userId)
    val account = Account(homePage, getUserUUID(userId))
    Agent(name = Option(user.getFullName), account = Option(account))
  }
}
