package cluster.start

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletHandler
import org.slf4j.LoggerFactory
import servlet.HelloWorldServlet

/**
  * Created by wenliu2 on 11/10/17.
  */

object ServerBootstrap {
    val logger = LoggerFactory.getLogger(ServerBootstrap.getClass)
    def main(args: Array[String]): Unit =
        if ( args.length != 1 ) usageAndExit() else startServer(args(0))

    //print usage and exit with -1
    def usageAndExit() {
        val className = ServerBootstrap.getClass.getCanonicalName
        logger.error(
            s"""Usage: ${className} port.
                """.stripMargin)
        System.exit(-1)
    }

    def startServer(port: String): Unit = {
        //startClusterStatus()
        startJettyServer(port)
    }

    // start embbed jetty server.
    def startJettyServer(port: String): Unit ={
        val jettyServer = new Server(port.toInt)

        val handler = new ServletHandler();
        jettyServer.setHandler(handler)

        handler.addServletWithMapping(classOf[HelloWorldServlet], "/hello")

        jettyServer.start()
        jettyServer.join()
    }
}


