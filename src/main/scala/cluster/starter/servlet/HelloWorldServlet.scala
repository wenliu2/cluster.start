package cluster.starter.servlet

import javax.servlet.RequestDispatcher
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import cluster.starter.cluster.ClusterStatus

/**
  * Created by wenliu2 on 11/10/17.
  */
class HelloWorldServlet extends HttpServlet{
    override def doGet(req: HttpServletRequest, response: HttpServletResponse): Unit ={
        //RequestDispatcher

        println("Got request from: " + req.getRemoteAddr)
        val role = if ( ClusterStatus.getStatus.hasLeadership )  "master" else "slave"
        val port = ClusterStatus.getStatus.getConfig.port
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(s"<h1>Hello from HelloServlet, role: ${role}, port: ${port}</h1>");
    }
}
