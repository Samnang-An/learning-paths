package com.arcusys.valamis.learningpath.listeners

import com.arcusys.valamis.learningpath.Configuration

class LessonMessageListener extends
  BaseCompletedMessageListener("lesson", Configuration.lessonListener)