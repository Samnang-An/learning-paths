package com.arcusys.valamis.learningpath

import com.arcusys.valamis.lrs.api.valamis.{ScaleApi, VerbApi}
import com.arcusys.valamis.lrs.api.{ActivityApi, ActivityProfileApi, StatementApi}
import com.arcusys.valamis.lrssupport.lrs.service.LrsClientManager

/**
  * Created by pkornilov on 3/28/17.
  */
class LrsClientTestImpl extends LrsClientManager {

  override def statementApi[T](action: (StatementApi) => T, authInfo: Option[String])(implicit companyId: Long): T = ???

  override def verbApi[T](action: (VerbApi) => T, authInfo: Option[String])(implicit companyId: Long): T = ???

  override def scaleApi[T](action: (ScaleApi) => T, authInfo: Option[String])(implicit companyId: Long): T = ???

  override def activityProfileApi[T](action: (ActivityProfileApi) => T, authInfo: Option[String])(implicit companyId: Long): T = ???

  override def activityApi[T](action: (ActivityApi) => T, authInfo: Option[String])(implicit companyId: Long): T = ???
}
