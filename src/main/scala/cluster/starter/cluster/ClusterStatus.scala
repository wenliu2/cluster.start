package cluster.starter.cluster

import java.net.InetAddress

import org.apache.curator.framework.recipes.leader.{LeaderLatch, LeaderLatchListener}
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.curator.x.discovery.details.JsonInstanceSerializer
import org.apache.curator.x.discovery.{ServiceDiscovery, ServiceDiscoveryBuilder, ServiceInstance, UriSpec}
import org.slf4j.LoggerFactory

case class NodeConfig(nodeId: String, port: String, latchPath: String = "/clusterstart/servers", zkConnection: String = "127.0.0.1:2181")

object ClusterStatus {
    private var clusterStatus: ClusterStatus = null

    def init(config: NodeConfig): Unit = {
        clusterStatus = new ClusterStatus(config)
        clusterStatus.init
    }

    def getStatus = this.clusterStatus
}

class ClusterStatus(config: NodeConfig) extends LeaderLatchListener {
    final val logger = LoggerFactory.getLogger(ClusterStatus.getClass)

    def getConfig = config

    val client: CuratorFramework = CuratorFrameworkFactory.newClient(
        config.zkConnection,
        //set session expiration time to 5 seconds, by default, the range defined in server is [4, 40] seconds.
        //this configuration defines how fast the new leader is elected if the old one is gone.
        5 * 1000,
        2 * 1000,
        new ExponentialBackoffRetry(1000, 3))
    private var leaderLatch: LeaderLatch = null
    private var discovery: ServiceDiscovery[InstanceDetails] = null
    private var masterServiceInstance: ServiceInstance[InstanceDetails] = null
    private var slaveServiceInstance: ServiceInstance[InstanceDetails] = null
    private val payload = new InstanceDetails(false)

    private final val serializer = new JsonInstanceSerializer(classOf[InstanceDetails])


    def init() = {
        client.start()
        client.blockUntilConnected()

        val serverHost = InetAddress.getLocalHost()
        val serverIp = serverHost.getHostAddress
        logger.info(s"server ip is ${serverIp}")

        this.masterServiceInstance = ServiceInstance.builder[InstanceDetails]()
            .uriSpec(new UriSpec("{scheme}://{address}:{port}"))
            .address(serverIp)
            .port(config.port.toInt)
            .name("masterService")
            .payload(payload)
            .build()

        this.slaveServiceInstance = ServiceInstance.builder[InstanceDetails]()
            .uriSpec(new UriSpec("{scheme}://{address}:{port}"))
            .address(serverIp)
            .port(config.port.toInt)
            .name("slaveService")
            .payload(payload)
            .build()

        discovery = ServiceDiscoveryBuilder.builder(classOf[InstanceDetails])
            .basePath("service-discovery")
            .client(client)
            //.thisInstance(this.serviceInstance)
            .watchInstances(true)
            .serializer(serializer)
            .build()

        discovery.start()

        discovery.registerService(this.slaveServiceInstance)

        leaderLatch = new LeaderLatch(client, config.latchPath, config.nodeId)
        leaderLatch.addListener(this)
        leaderLatch.start
    }

    def hasLeadership = leaderLatch.hasLeadership

    override def isLeader = {
        logger.info(s"${config.nodeId} is leader now.")
        payload.setLeader(true)
        //discovery.updateService(serviceInstance)
        discovery.registerService(this.masterServiceInstance)
        discovery.unregisterService(this.slaveServiceInstance)
    }

    override def notLeader() = {
        logger.info(s"${config.nodeId} is not a leader.")
        payload.setLeader(false)
        discovery.registerService(this.slaveServiceInstance)
        discovery.unregisterService(this.masterServiceInstance)
    }

    def close(): Unit ={
        leaderLatch.close()
        discovery.close()
        client.close()
    }
}
