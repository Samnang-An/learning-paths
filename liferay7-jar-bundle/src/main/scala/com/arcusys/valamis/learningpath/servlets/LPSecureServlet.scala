package com.arcusys.valamis.learningpath.servlets

import javax.servlet.http.HttpServlet

import com.liferay.portal.kernel.servlet.{PortalDelegatorServlet, SecureServlet}
import org.osgi.service.component.annotations.Component

/**
  * Created by mminin on 17/02/2017.
  */
@Component(
  name = "com.arcusys.valamis.learningpath.servlets.LPSecureServlet",
  service = Array(classOf[javax.servlet.Servlet]),
  property = Array(
    "osgi.http.whiteboard.servlet.name=learning-paths",
    "osgi.http.whiteboard.servlet.pattern=/learning-paths/*",
    "osgi.http.whiteboard.servlet.load-on-startup=1",
    "servlet.init.sub-context=learning-paths"
  )
)
class LPSecureServlet extends SecureServlet {
  val contextPath = "learning-paths"

  servlet = new MainServlet(s"/$contextPath/")

  override protected def doPortalInit(): Unit = {
    servlet.init(servletConfig)

    PortalDelegatorServlet.addDelegate(contextPath, servlet.asInstanceOf[HttpServlet])
  }

  override protected def doPortalDestroy() {
    PortalDelegatorServlet.removeDelegate(contextPath)
    
    servlet.destroy()
  }
}
