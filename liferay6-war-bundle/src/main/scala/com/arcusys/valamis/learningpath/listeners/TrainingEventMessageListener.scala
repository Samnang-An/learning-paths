package com.arcusys.valamis.learningpath.listeners

import com.arcusys.valamis.learningpath.Configuration

class TrainingEventMessageListener extends
  BaseCompletedMessageListener("event", Configuration.trainingEventListener)