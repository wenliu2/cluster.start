package cluster.start

import org.apache.log4j.Logger
import org.apache.log4j.spi.LoggerFactory
import org.eclipse.jetty.server.Server

/**
  * Created by wenliu2 on 11/10/17.
  */

object ServerBootstrap {
    val logger = Logger.getLogger(ServerBootstrap.getClass)
    def main(args: Array[String]): Unit = {
        if ( args.length != 1 ){
            val className = ServerBootstrap.getClass.getCanonicalName
            logger.error(
                s"""Usage: ${className} port.
                """.stripMargin)
            System.exit(-1)
        }else{
            startServer(args(0))
        }
    }

    def startServer(port: String): Unit ={
        val jettyServer = new Server(port.toInt)

        logger.debug("start server")
        jettyServer.start()
        jettyServer.join()

    }

}
