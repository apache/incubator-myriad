package com.ebay.myriad.scheduler.fgs

import com.ebay.myriad.scheduler.yarn.interceptor.InterceptorRegistry
import com.ebay.myriad.state.SchedulerState
import org.apache.hadoop.yarn.api.records.ContainerState
import org.apache.hadoop.yarn.api.records.ContainerStatus
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNode
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNodeStartedEvent
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNodeStatusEvent
import org.apache.hadoop.yarn.util.resource.Resources
import org.apache.mesos.Protos
import org.slf4j.Logger

/**
 *
 * Tests for NMHeartBeatHandler
 *
 */
class NMHeartBeatHandlerSpec extends FGSTestBaseSpec {

  def "Node Manager registration"() {
    given:
    def hbHandler = getNMHeartBeatHandler()
    hbHandler.logger = Mock(Logger)

    def nonZeroNM = getRMNode(2, 2048, "test_host1", null)
    def zeroNM = getRMNode(0, 0, "test_host2", null)

    when:
    hbHandler.beforeRMNodeEventHandled(getNMRegistrationEvent(nonZeroNM), rmContext)

    then:
    1 * hbHandler.logger.warn('FineGrainedScaling feature got invoked for a NM with non-zero capacity. ' +
        'Host: {}, Mem: {}, CPU: {}. Setting the NM\'s capacity to (0G,0CPU)', 'test_host1', 2048, 2)
    nonZeroNM.getTotalCapability().getMemory() == 0
    nonZeroNM.getTotalCapability().getVirtualCores() == 0

    when:
    hbHandler.beforeRMNodeEventHandled(getNMRegistrationEvent(zeroNM), rmContext)

    then:
    0 * hbHandler.logger.warn(_) // no logger.warn invoked
    nonZeroNM.getTotalCapability().getMemory() == 0
    nonZeroNM.getTotalCapability().getVirtualCores() == 0
  }

  def "Node Manager HeartBeat"() {
    given:
    def host = "test_host"
    def slaveId = Protos.SlaveID.newBuilder().setValue(host + "_slave_id").build()
    def zeroNM = getRMNode(0, 0, host, slaveId)

    def fgsContainer1 = getFGSContainer(zeroNM, 1, 1, 1024, ContainerState.RUNNING)
    def fgsContainer2 = getFGSContainer(zeroNM, 2, 1, 1024, ContainerState.COMPLETE)
    def fgsContainer3 = getFGSContainer(zeroNM, 3, 1, 1024, ContainerState.RUNNING)

    addOfferToFeed(slaveId, host, 2, 2048)

    def yarnNodeCapacityManager = Mock(YarnNodeCapacityManager)
    def hbHandler = getNMHeartBeatHandler(yarnNodeCapacityManager)

    when:
    hbHandler.handleStatusUpdate(
        getHBEvent(
            zeroNM,
            fgsContainer1.containerStatus,
            fgsContainer2.containerStatus,
            fgsContainer3.containerStatus),
        rmContext)

    then:
    nodeStore.getNode(host).getContainerSnapshot().size() == 3
    1 * yarnNodeCapacityManager.setNodeCapacity(zeroNM, Resources.createResource(4096, 4))
  }


  RMNodeStartedEvent getNMRegistrationEvent(RMNode node) {
    new RMNodeStartedEvent(node.getNodeID(), null, null)
  }

  RMNodeStatusEvent getHBEvent(RMNode node, ContainerStatus... statuses) {
    return new RMNodeStatusEvent(node.getNodeID(), null, Arrays.asList(statuses), null, null)
  }

  NMHeartBeatHandler getNMHeartBeatHandler() {
    return getNMHeartBeatHandler(Mock(YarnNodeCapacityManager))
  }

  NMHeartBeatHandler getNMHeartBeatHandler(YarnNodeCapacityManager yarnNodeCapacityMgr) {
    def registry = Mock(InterceptorRegistry)
    def state = Mock(SchedulerState)
    return new NMHeartBeatHandler(registry, yarnScheduler, myriadDriver,
        yarnNodeCapacityMgr, offerLifecycleManager, nodeStore, state)
  }

}
