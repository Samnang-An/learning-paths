package com.arcusys.valamis.learningpath.listeners.model

/**
  * Created by pkornilov on 4/4/17.
  */
case class LPInfo(id: Long,
                  companyId: Long,
                  activated: Boolean,
                  title: String,
                  description: Option[String],
                  logoUrl: Option[String])