package cluster.starter

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{ServletContextHandler, ServletHandler}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Created by wenliu2 on 11/13/17.
  */
abstract class BaseBootstrap[ARG <: BaseArgument] {
    private var _arguments: ARG = null.asInstanceOf[ARG]
    val shutdownCodes = mutable.Buffer[() => Unit]()

    def getLogger: Logger

    def arguments = _arguments

    def main(args: Array[String]) = {
        val parseResult = parseArgument(args)
        if ( parseResult.isLeft ){
            usageAndExit(parseResult.left.get)
        }else {
            this._arguments = parseResult.right.get
            shutdownHook()
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

        addShutdownHook{
            getLogger.info("stopping server ...")
            jettyServer.stop()
        }

        jettyServer.join()
        getLogger.info("server started, after join.")
    }

    def addShutdownHook(code:  => Unit): Unit ={
        shutdownCodes += ( () => code )
    }

    def shutdownHook(): Unit ={
        Runtime.getRuntime.addShutdownHook(new Thread(){
            override def run(): Unit = {
                shutdownCodes.foreach{_()}
            }
        })
    }
}

class BaseArgument(port: Int){
    def getPort = port
}
