package com.arcusys.valamis.learningpath.impl.utils

import com.liferay.portal.kernel.{log => liferay}
import org.apache.commons.logging.Log

/**
  * Created by mminin on 07/02/2017.
  */
trait LiferayLogSupport {
  protected val log: Log = {
    new LogWrapper(liferay.LogFactoryUtil.getLog(this.getClass))
  }
}

class LogWrapper(liferayLog: liferay.Log) extends Log {
  override def warn(message: scala.Any): Unit = liferayLog.warn(message)

  override def warn(message: scala.Any, t: Throwable): Unit = liferayLog.warn(message, t)

  override def isErrorEnabled: Boolean = liferayLog.isErrorEnabled

  override def isInfoEnabled: Boolean = liferayLog.isInfoEnabled

  override def isDebugEnabled: Boolean = liferayLog.isDebugEnabled

  override def isTraceEnabled: Boolean = liferayLog.isTraceEnabled

  override def error(message: scala.Any): Unit = liferayLog.error(message)

  override def error(message: scala.Any, t: Throwable): Unit = liferayLog.error(message, t)

  override def debug(message: scala.Any): Unit = liferayLog.debug(message)

  override def debug(message: scala.Any, t: Throwable): Unit = liferayLog.debug(message, t)

  override def fatal(message: scala.Any): Unit = liferayLog.fatal(message)

  override def fatal(message: scala.Any, t: Throwable): Unit = liferayLog.fatal(message, t)

  override def isWarnEnabled: Boolean = liferayLog.isWarnEnabled

  override def trace(message: scala.Any): Unit = liferayLog.trace(message)

  override def trace(message: scala.Any, t: Throwable): Unit = liferayLog.trace(message, t)

  override def info(message: scala.Any): Unit = liferayLog.info(message)

  override def info(message: scala.Any, t: Throwable): Unit = liferayLog.info(message, t)

  override def isFatalEnabled: Boolean = liferayLog.isFatalEnabled
}