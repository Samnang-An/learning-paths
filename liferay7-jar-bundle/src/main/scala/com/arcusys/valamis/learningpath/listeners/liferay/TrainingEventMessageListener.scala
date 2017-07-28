package com.arcusys.valamis.learningpath.listeners.liferay

import com.arcusys.valamis.learningpath.init.Configuration

class TrainingEventMessageListener extends
  BaseCompletedMessageListener("event", Configuration.trainingEventListener)