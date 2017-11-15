package cluster.starter.servlet

import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import cluster.starter.DispatcherBootstrap
import org.slf4j.LoggerFactory

/**
  * Created by wenliu2 on 11/13/17.
  */
class DispatcherInfoServlet extends HttpServlet{
    final val logger = LoggerFactory.getLogger(classOf[DispatcherInfoServlet])
    override def service(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
        //super.service(req, resp)
        logger.info("dispatch servlet is called.")
        val pathInfo = req.getPathInfo
        val contextPath = req.getContextPath
        val queryString = req.getQueryString
        val pathTranlated = req.getPathTranslated
        val requestURI = req.getRequestURI
        val requestURL = req.getRequestURL
        val servletPath = req.getServletPath

        val masterService = DispatcherBootstrap.getMaster.getInstance()
        val slaveService = DispatcherBootstrap.getSlave.getInstance()

        val (masterAddress, masterPort) = if ( masterService == null ) ("", "") else (masterService.getAddress, masterService.getPort)
        val (slaveAddress, slavePort) = if ( slaveService == null ) ("", "") else (slaveService.getAddress, slaveService.getPort)

        val info =
            s"""
              |pathInfo: ${pathInfo} <br>
              |contextPath: ${contextPath} <br>
              |queryString: ${queryString} <br>
              |pathTranslated: ${pathTranlated} <br>
              |requestURI: ${requestURI} <br>
              |requestURL: ${requestURL} <br>
              |servletPath: ${servletPath} <br>
              |master: ${masterAddress}:${masterPort} <br>
              |slave: ${slaveAddress}:${slavePort} <br>
              |
            """.stripMargin
        logger.info(info)
        resp.setContentType("text/html");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().println(s"${info}");
    }
}
