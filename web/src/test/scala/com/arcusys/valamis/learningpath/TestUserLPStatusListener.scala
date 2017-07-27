package com.arcusys.valamis.learningpath

import com.arcusys.valamis.learningpath.listeners.UserLPStatusListener
import com.arcusys.valamis.learningpath.models.{LPVersion, LearningPath}

/**
  * Created by pkornilov on 6/22/17.
  */
class TestUserLPStatusListener extends UserLPStatusListener {
  override private[learningpath] def onCompleted(userId: Long,
                                                 lp: LearningPath,
                                                 version: LPVersion)
                                                (implicit companyId: Long): Unit = {}

  override private[learningpath] def onFailed(userId: Long,
                                              lp: LearningPath,
                                              version: LPVersion)
                                             (implicit companyId: Long): Unit = {}
}
