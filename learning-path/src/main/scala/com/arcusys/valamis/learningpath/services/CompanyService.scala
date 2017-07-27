package com.arcusys.valamis.learningpath.services

/**
  * Created by pkornilov on 3/24/17.
  */
trait CompanyService {
  def getCompanyDefaultUserId(companyId: Long): Long
}
