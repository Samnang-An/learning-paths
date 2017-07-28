package com.arcusys.valamis.learningpath.listeners.liferay

import com.arcusys.valamis.learningpath.init.Configuration

class CourseMessageListener extends
  BaseCompletedMessageListener("course", Configuration.courseListener)