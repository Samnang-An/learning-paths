package com.arcusys.valamis.learningpath.listeners.liferay

import com.arcusys.valamis.learningpath.init.Configuration

class LessonMessageListener extends
  BaseCompletedMessageListener("lesson", Configuration.lessonListener)