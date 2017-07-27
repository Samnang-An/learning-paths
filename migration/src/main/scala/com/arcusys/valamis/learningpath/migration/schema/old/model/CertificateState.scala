package com.arcusys.valamis.learningpath.migration.schema.old.model

import com.arcusys.valamis.learningpath.models.CertificateStatuses//the same as in old curriculum
import org.joda.time.DateTime

private[migration] case class CertificateState(
  userId: Long,
  status: CertificateStatuses.Value,
  statusAcquiredDate: DateTime,
  userJoinedDate: DateTime,
  certificateId: Long
)