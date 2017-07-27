package com.arcusys.valamis.learningpath.impl

import com.arcusys.valamis.learningpath.services.CompanyService
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil

/**
  * Created by pkornilov on 3/24/17.
  */
class CompanyServiceImpl extends CompanyService {

  override def getCompanyDefaultUserId(companyId: Long): Long = {
    CompanyLocalServiceUtil.getCompanyById(companyId).getDefaultUser.getUserId
  }
}
