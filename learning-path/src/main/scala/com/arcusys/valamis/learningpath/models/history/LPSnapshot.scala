package com.arcusys.valamis.learningpath.models.history

import com.arcusys.valamis.learningpath.models.{CertificateStatuses, PeriodTypes}
import org.joda.time.DateTime

/**
  * Created by mminin on 12/08/16.
  */
case class LPSnapshot(learningPathId: Long,
                      date: DateTime,
                      isDeleted: Boolean,
                      title: String,
                      isPermanent: Boolean,
                      companyId: Long,
                      validPeriodType: PeriodTypes.Value,
                      validPeriodValue: Int,
                      isPublished: Boolean,
                      scope: Option[Long])

case class UserStatusSnapshot(learningPathId: Long,
                              userId: Long,
                              status: CertificateStatuses.Value,
                              date: DateTime,
                              isDeleted: Boolean)