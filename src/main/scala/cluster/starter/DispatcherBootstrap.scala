package cluster.starter

import java.util.concurrent.Executors

import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.curator.x.discovery.{ProviderStrategy, ServiceDiscovery, ServiceDiscoveryBuilder, ServiceProvider}
import org.eclipse.jetty.servlet.{ServletContextHandler, ServletHandler, ServletHolder}
import org.slf4j.LoggerFactory
import servlet.DispatcherServlet
import servlet.DispatcherInfoServlet
import cluster.InstanceDetails
import org.apache.curator.x.discovery.details.JsonInstanceSerializer
import org.apache.curator.x.discovery.strategies.RoundRobinStrategy

/**
  * Created by wenliu2 on 11/13/17.
  */
case class DispatcherArgument(port: Int) extends BaseArgument(port)

object DispatcherBootstrap extends BaseBootstrap[DispatcherArgument]{
    final val logger = LoggerFactory.getLogger(DispatcherBootstrap.getClass)
    override def getLogger = logger

    private var masterServiceProvider: ServiceProvider[InstanceDetails] = null
    private var slaveServiceProvider: ServiceProvider[InstanceDetails] = null

    def getMaster = masterServiceProvider
    def getSlave = slaveServiceProvider

    override def parseArgument(args: Array[String]): Either[String, DispatcherArgument] = {
        if ( args.size != 1 ){
            val className = DispatcherBootstrap.getClass.getCanonicalName
            Left(s"""Usage: ${className} port""")
        } else {
            Right(DispatcherArgument(args(0).toInt))
        }
    }

    override def addServletWithMapping(handler: ServletContextHandler) = {
        val infoServletHolder = new ServletHolder("info", classOf[DispatcherInfoServlet])
        handler.setContextPath("/")

        handler.addServlet(infoServletHolder, "/info/*")

        val masterServletHolder = new ServletHolder("master", classOf[DispatcherServlet])
        handler.addServlet(masterServletHolder, "/master/*")

        val slaveServletHolder = new ServletHolder("slave", classOf[DispatcherServlet])
        handler.addServlet(slaveServletHolder, "/slave/*")
        handler.getServletContext.setAttribute("org.eclipse.jetty.server.Executor", Executors.newCachedThreadPool())
    }

    override def startCluster(): Unit = {
        val zkConnection = "127.0.0.1:2181"
        val client: CuratorFramework = CuratorFrameworkFactory.newClient(
            zkConnection,
            5 * 1000,
            2 * 1000,
            new ExponentialBackoffRetry(1000, 3))

        client.start()
        client.blockUntilConnected()

        val serializer = new JsonInstanceSerializer(classOf[InstanceDetails])
        val discovery: ServiceDiscovery[InstanceDetails] = ServiceDiscoveryBuilder.builder(classOf[InstanceDetails])
            .basePath("service-discovery")
            .client(client)
            //.thisInstance(this.serviceInstance)
            .watchInstances(true)
            .serializer(serializer)
            .build()

        discovery.start()
        this.masterServiceProvider = discovery.serviceProviderBuilder()
            .providerStrategy(new RoundRobinStrategy[InstanceDetails])
            .serviceName("masterService")
            .build()

        this.slaveServiceProvider = discovery.serviceProviderBuilder()
            .providerStrategy(new RoundRobinStrategy[InstanceDetails])
            .serviceName("slaveService")
            .build()

        this.masterServiceProvider.start()
        this.slaveServiceProvider.start()

        addShutdownHook{
            this.masterServiceProvider.close()
            this.slaveServiceProvider.close()
            discovery.close()
            client.close()
        }
    }
}
