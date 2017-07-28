package com.arcusys.valamis.learningpath

import com.arcusys.valamis.learningpath.models.{LPVersion, LearningPath, UserLPStatus}

/**
  * Created by pkornilov on 4/4/17.
  */
package object services {
  type LPWithVersionAndStatus = (LearningPath, LPVersion, Option[UserLPStatus])
  type LPWithVersionAndSucceededUserIds = (LearningPath, LPVersion, Seq[Long])
  type LPWithVersionAndSucceededUserCount = (LearningPath, LPVersion, Int)
}
