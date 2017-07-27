package com.arcusys.valamis.learningpath.models

case class WebContent(id: Long, title: String)

case class WebContents(items: Seq[WebContent], total: Long)

object WebContentSort extends Enumeration {
  val title = Value("title")
  val titleDesc = Value("-title")
}