package com.arcusys.valamis.learningpath.listeners

import com.arcusys.valamis.learningpath.Configuration

class CourseMessageListener extends
  BaseCompletedMessageListener("course", Configuration.courseListener)