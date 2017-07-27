package com.arcusys.valamis.learningpath.listeners

import com.arcusys.valamis.learningpath.models.{LPVersion, LearningPath}

/**
  * Created by pkornilov on 6/22/17.
  */
trait UserLPStatusListener {

  private[learningpath] def onCompleted(userId: Long,
                                        lp: LearningPath,
                                        version: LPVersion)
                                       (implicit companyId: Long): Unit

  private[learningpath] def onFailed(userId: Long,
                                     lp: LearningPath,
                                     version: LPVersion)
                                    (implicit companyId: Long): Unit
}