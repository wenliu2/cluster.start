package cluster.start

import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.framework.recipes.leader.{LeaderLatch, LeaderLatchListener}
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.curator.x.discovery.ServiceDiscovery
import org.slf4j.LoggerFactory

case class NodeConfig(nodeId: String, port: String, latchPath: String = "/clusterstart/servers", zkConnection: String = "127.0.0.1:2181")

object ClusterStatus{
  private var clusterStatus: ClusterStatus = null
  def init(config: NodeConfig): Unit = {
    clusterStatus = new ClusterStatus(config)
    clusterStatus.init
  }
  def getStatus = this.clusterStatus
}

class ClusterStatus(config: NodeConfig) extends LeaderLatchListener{
  final val logger = LoggerFactory.getLogger(ClusterStatus.getClass)

  val client: CuratorFramework = CuratorFrameworkFactory.newClient(
    config.zkConnection,
    //set session expiration time to 5 seconds, by default, the range defined in server is [4, 40] seconds.
    //this configuration defines how fast the new leader is elected if the old one is gone.
    5*1000,
    2*1000,
    new ExponentialBackoffRetry(1000,3))
  private var leaderLatch: LeaderLatch = null
  //var discovery: ServiceDiscovery[InstanceDetails] = null


  def init() = {
    client.start()
    client.blockUntilConnected()
    leaderLatch = new LeaderLatch(client, config.latchPath, config.nodeId)
    leaderLatch.addListener(this)
    leaderLatch.start
  }

  def hasLeadership = leaderLatch.hasLeadership

  override def isLeader = {
    logger.info(s"${config.nodeId} is leader now.")
  }

  override def notLeader() = {
    logger.info(s"${config.nodeId} is not a leader.")
  }
}
