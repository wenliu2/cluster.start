package cluster.starter

import java.util.concurrent.Executors

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{ServletContextHandler, ServletHandler, ServletHolder}
import org.slf4j.LoggerFactory
import servlet.HelloWorldServlet
import cluster._

/**
  * Created by wenliu2 on 11/10/17.
  */

case class ServerArg(port: Int) extends BaseArgument(port)
object ServerBootstrap extends BaseBootstrap[ServerArg]{
    val logger = LoggerFactory.getLogger(ServerBootstrap.getClass)

    override def getLogger = logger
    //private var jettyServer: Server = null
    /*
    def main(args: Array[String]): Unit =
        if ( args.length != 1 ) usageAndExit() else startServer(args(0))
    */

    override def parseArgument(args: Array[String]) = {
        val className = ServerBootstrap.getClass.getCanonicalName
        val msg = s"""Usage: ${className} port.""".stripMargin
        if ( args.length != 1 ) Left(msg) else Right(ServerArg(args(0).toInt))
    }

    override def startCluster() = {
        ClusterStatus.init(NodeConfig(s"nodeId-${this.arguments.port}", this.arguments.port))
        addShutdownHook{
            ClusterStatus.getStatus.close()
        }
    }

    override def addServletWithMapping(handler: ServletContextHandler) = {
        val infoServletHolder = new ServletHolder("helloworld", classOf[HelloWorldServlet])
        handler.setContextPath("/")

        handler.addServlet(infoServletHolder, "/hello")
        handler.getServletContext.setAttribute("org.eclipse.jetty.server.Executor", Executors.newCachedThreadPool())

    }
}


