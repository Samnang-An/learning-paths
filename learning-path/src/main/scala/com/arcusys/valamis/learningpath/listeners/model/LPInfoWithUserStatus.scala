package com.arcusys.valamis.learningpath.listeners.model

import com.arcusys.valamis.learningpath.models.CertificateStatuses
import org.joda.time.{DateTime, Period}

/**
  * Created by pkornilov on 4/4/17.
  */
case class LPInfoWithUserStatus(id: Long,
                                companyId: Long,
                                activated: Boolean,
                                title: String,
                                validPeriod: Option[Period],
                                description: Option[String],
                                logoUrl: Option[String],
                                status: Option[CertificateStatuses.Value],
                                statusDate: Option[DateTime],
                                progress: Option[Double])