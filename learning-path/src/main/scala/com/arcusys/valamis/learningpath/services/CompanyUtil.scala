package com.arcusys.valamis.learningpath.services

/**
  * Created by eboytsova on 17/05/2017.
  */
trait CompanyUtil {
  def getHomePage(companyId: Long): String

  def getHostWithPort(companyId: Long, isSecure: Boolean = false): String
}
