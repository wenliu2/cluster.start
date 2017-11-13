package cluster.starter.servlet

import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import cluster.starter.cluster.ClusterStatus

/**
  * Created by wenliu2 on 11/10/17.
  */
class HelloWorldServlet extends HttpServlet{
    override def doGet(req: HttpServletRequest, response: HttpServletResponse): Unit ={
        val role = if ( ClusterStatus.getStatus.hasLeadership )  "master" else "slave"
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(s"<h1>Hello from HelloServlet, (${role})</h1>");
    }
}
