package cluster.starter.cluster

import org.codehaus.jackson.map.annotate.JsonRootName

/**
  * Created by wenliu2 on 11/13/17.
  */
@JsonRootName("details")
class InstanceDetails(var leader: Boolean) {
    def this() = this(false)

    def isLeader() = leader
    def setLeader(isLeader: Boolean) = this.leader = isLeader
}
