package com.arcusys.valamis.learningpath.init

import org.osgi.framework.BundleContext
import slick.util.GlobalConfig

class SlickActivator {

  def start(context: BundleContext): Unit = {

    // init slick GlobalConfig with correct classloader
    // Thread.currentThread.getContextClassLoader can not read resources
    val oldClassLoader = Thread.currentThread.getContextClassLoader
    try {
      Thread.currentThread.setContextClassLoader(this.getClass.getClassLoader)
      // read test property should return empty config, config will be read
      GlobalConfig.driverConfig("test")
    } finally {
      Thread.currentThread.setContextClassLoader(oldClassLoader)
    }
  }

}
