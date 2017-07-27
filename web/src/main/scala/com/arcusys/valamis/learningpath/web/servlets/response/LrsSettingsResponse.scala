package com.arcusys.valamis.learningpath.web.servlets.response

case class LrsAccount(name: String,
                      homePage: String)

case class LrsAgent(name: String,
                    account: LrsAccount,
                    objectType: String = "Agent")

case class LrsSettingsResponse(valamisContextPath: String,
                               endpoint: String,
                               auth: String,
                               agent: LrsAgent)
