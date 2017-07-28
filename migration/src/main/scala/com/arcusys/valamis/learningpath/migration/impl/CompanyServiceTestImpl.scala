package com.arcusys.valamis.learningpath.migration.impl

import com.arcusys.valamis.learningpath.services.CompanyService

/**
  * Created by pkornilov on 3/28/17.
  */
class CompanyServiceTestImpl(userMap: Map[Long, Long]) extends CompanyService {

  override def getCompanyDefaultUserId(companyId: Long): Long =
    userMap.getOrElse(companyId,
      throw new NoSuchElementException("no default user id for company: " + companyId))

}
