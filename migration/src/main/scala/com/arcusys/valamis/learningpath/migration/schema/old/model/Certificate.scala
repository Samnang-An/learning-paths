package com.arcusys.valamis.learningpath.migration.schema.old.model

import com.arcusys.valamis.learningpath.migration.schema.old.model.PeriodTypes.PeriodType
import org.joda.time.DateTime

/**
 * Certificate to be passed by user. Contains list of sites.
 *
 * @param id                  Unique internal ID
 * @param title               Short title
 * @param description         More detailed description
 */
private[migration] case class Certificate(id: Long,
                       title: String,
                       description: String,
                       logo: String = "",
                       isPermanent: Boolean = true,
                       isPublishBadge: Boolean = false,
                       shortDescription: String = "",
                       companyId: Long,
                       validPeriodType: PeriodType = PeriodTypes.UNLIMITED,
                       validPeriod: Int = 0,
                       createdAt: DateTime,
                       activationDate: Option[DateTime] = None,
                       isActive: Boolean = false,
                       scope: Option[Long] = None)
