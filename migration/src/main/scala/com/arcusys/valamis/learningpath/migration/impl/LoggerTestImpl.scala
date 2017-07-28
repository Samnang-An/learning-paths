package com.arcusys.valamis.learningpath.migration.impl

import org.apache.commons.logging.Log

class LoggerTestImpl extends Log {
  override def isErrorEnabled: Boolean = true

  override def isInfoEnabled: Boolean = true

  override def isDebugEnabled: Boolean = true

  override def isTraceEnabled: Boolean = true

  override def isWarnEnabled: Boolean = true

  override def isFatalEnabled: Boolean = true

  override def warn(message: scala.Any): Unit =
    println("WARN: " + message)

  override def warn(message: scala.Any, t: Throwable): Unit =
    println("WARN: " + message + " " + t.getMessage)


  override def error(message: scala.Any): Unit =
    if (isErrorEnabled) println("ERROR: " + message)

  override def error(message: scala.Any, t: Throwable): Unit = if (isErrorEnabled){
    println("ERROR: " + t.getClass.getName + " " + message + " " + t.getMessage)
    t.printStackTrace()
  }

  override def debug(message: scala.Any): Unit =
    println("DEBUG: " + message)

  override def debug(message: scala.Any, t: Throwable): Unit =
    println("DEBUG: " + message + " " + t.getMessage)


  override def fatal(message: scala.Any): Unit =
    println("FATAL: " + message)

  override def fatal(message: scala.Any, t: Throwable): Unit =
    println("FATAL: " + message + " " + t.getMessage)


  override def trace(message: scala.Any): Unit =
    println("TRACE: " + message)

  override def trace(message: scala.Any, t: Throwable): Unit =
    println("TRACE: " + message + " " + t.getMessage)


  override def info(message: scala.Any): Unit =
    println("INFO: " + message)

  override def info(message: scala.Any, t: Throwable): Unit =
    println("INFO: " + message + " " + t.getMessage)
}
