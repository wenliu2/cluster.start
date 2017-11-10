package cluster.start.servlet

import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

/**
  * Created by wenliu2 on 11/10/17.
  */
class HelloWorldServlet extends HttpServlet{
    override def doGet(req: HttpServletRequest, response: HttpServletResponse): Unit ={
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("<h1>Hello from HelloServlet</h1>");
    }
}
