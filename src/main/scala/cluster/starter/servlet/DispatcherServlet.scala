package cluster.starter.servlet

import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import cluster.starter.DispatcherBootstrap
import cluster.starter.cluster.InstanceDetails
import org.apache.curator.x.discovery.{ServiceInstance, ServiceProvider}
import org.eclipse.jetty.proxy.ProxyServlet
import org.slf4j.LoggerFactory

/**
  * Created by wenliu2 on 11/13/17.
  */
class DispatcherServlet extends ProxyServlet{
    final val logger = LoggerFactory.getLogger(classOf[DispatcherServlet])
    /*
    override def service(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
        super.service(req, resp)
    }
    */

    //def getServiceName: String = ???
    def getServiceInstance(serviceName: String): ServiceInstance[InstanceDetails] = {
       serviceName match{
           case "/master" => DispatcherBootstrap.getMaster.getInstance()
           case "/slave" => DispatcherBootstrap.getSlave.getInstance()
           case _ => null
       }
    }
    override def rewriteTarget(clientRequest: HttpServletRequest) = {
        val servletPath = clientRequest.getServletPath
        val si = getServiceInstance(servletPath)
        if ( null == si ){
            logger.warn(s"service instance '${servletPath}' is null, please check backend service.")
            null;
        }else{
            val host = si.getAddress + ":" + si.getPort
            val pathInfo = clientRequest.getPathInfo
            val queryString = clientRequest.getQueryString
            val newUrl = pathInfo + ( if ( null != queryString ) "?" + queryString else "" )

            val target = "http://" + host + newUrl
            logger.info(s"target: ${target}")
            target
        }
    }
}
