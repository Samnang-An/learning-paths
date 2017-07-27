package com.arcusys.valamis.learningpath.models

import com.arcusys.valamis.members.picker.model.SkipTake
import org.joda.time.DateTime

case class CertificateTrackerFilter(startDate: DateTime,
                                    endDate: DateTime,
                                    scope: Option[Long] = None,
                                    userIds: Seq[Long] = Seq(),
                                    skipTake: Option[SkipTake] = None,
                                    certificateIds: Seq[Long] =  Seq())