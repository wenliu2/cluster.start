package cluster.starter

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{ServletContextHandler, ServletHandler}
import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by wenliu2 on 11/13/17.
  */
abstract class BaseBootstrap[ARG <: BaseArgument] {
    private var _arguments: ARG = null.asInstanceOf[ARG]

    def getLogger: Logger

    def arguments = _arguments

    def main(args: Array[String]) = {
        val parseResult = parseArgument(args)
        if ( parseResult.isLeft ){
            usageAndExit(parseResult.left.get)
        }else {
            this._arguments = parseResult.right.get
            startCluster()
            startServer(arguments.getPort)
        }
    }

    def usageAndExit(message: String) = {
        println(message)
        System.exit(-1)
    }

    def parseArgument(args: Array[String]): Either[String, ARG]

    def addServletWithMapping(handler: ServletContextHandler)

    def startCluster()

    def startServer(port: Int): Unit ={
        val jettyServer = new Server(port)

        val handler = new ServletContextHandler();
        jettyServer.setHandler(handler)

        //handler.addServletWithMapping(classOf[HelloWorldServlet], "/hello")
        addServletWithMapping(handler)

        jettyServer.start()
        jettyServer.join()
    }
}

class BaseArgument(port: Int){
    def getPort = port
}
