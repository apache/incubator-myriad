/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myriad.scheduler.fgs

import org.apache.hadoop.yarn.api.records.ContainerState
import org.apache.hadoop.yarn.api.records.ResourceOption
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNode
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.NodeResourceUpdateSchedulerEvent
import org.apache.hadoop.yarn.util.resource.Resources
import org.apache.mesos.Protos
import org.apache.myriad.configuration.MyriadConfiguration
import org.apache.myriad.configuration.NodeManagerConfiguration
import org.apache.myriad.scheduler.TaskUtils
import org.apache.myriad.scheduler.yarn.interceptor.InterceptorRegistry
import org.apache.myriad.state.NodeTask
import org.apache.myriad.state.SchedulerState

/**
 *
 * Tests for YarnNodeCapacityManager
 *
 */
class YarnNodeCapacityManagerSpec extends FGSTestBaseSpec {

    def "No Containers Allocated Due To Mesos Offers"() {
        given:
        def yarnNodeCapacityMgr = getYarnNodeCapacityManager()

        def host = "test_host"
        def slaveId = Protos.SlaveID.newBuilder().setValue(host + "_slave_id").build()
        def zeroNM = getRMNode(0, 0, host, slaveId)

        // have a mesos offer before HB
        def offer = addOfferToFeed(slaveId, host, 4, 4096)
        offerLifecycleManager.markAsConsumed(offer)

        //  2 containers before HB.
        def fgsContainer1 = getFGSContainer(zeroNM, 1, 1, 1024, ContainerState.RUNNING)
        def fgsContainer2 = getFGSContainer(zeroNM, 2, 1, 1024, ContainerState.RUNNING)
        nodeStore.getNode(host).snapshotRunningContainers()

        // Node's capacity set to match the size of 2 containers + mesos offers
        yarnNodeCapacityMgr.setNodeCapacity(zeroNM, Resources.createResource(6144, 6))

        // no new container allocations

        when:
        yarnNodeCapacityMgr.handleContainerAllocation(zeroNM)

        then:
        nodeStore.getNode(host).getNode().getRunningContainers().size() == 2  // 2 containers still running
        1 * mesosDriver.declineOffer(offer.getId())   // offer rejected, as it's not used to allocate more containers
        zeroNM.getTotalCapability().getVirtualCores() == 2 // capacity returns back to match size of running containers
        zeroNM.getTotalCapability().getMemory() == 2048
        nodeStore.getNode(host).getContainerSnapshot() == null // container snapshot is released
    }

    def "Containers Allocated Due To Mesos Offers"() {
        given:
        def yarnNodeCapacityMgr = getYarnNodeCapacityManager()

        def host = "test_host"
        def slaveId = Protos.SlaveID.newBuilder().setValue(host + "_slave_id").build()
        def zeroNM = getRMNode(0, 0, host, slaveId)

        // have a mesos offer before HB
        def offer = addOfferToFeed(slaveId, host, 4, 4096)
        offerLifecycleManager.markAsConsumed(offer)

        //  2 containers before HB.
        def fgsContainer1 = getFGSContainer(zeroNM, 1, 1, 1024, ContainerState.RUNNING)
        def fgsContainer2 = getFGSContainer(zeroNM, 2, 1, 1024, ContainerState.RUNNING)
        nodeStore.getNode(host).snapshotRunningContainers()

        // Node's capacity set to match the size of 2 running containers + mesos offers
        yarnNodeCapacityMgr.setNodeCapacity(zeroNM, Resources.createResource(6144, 6))

        // 2 new containers allocated after HB
        def fgsContainer3 = getFGSContainer(zeroNM, 3, 1, 1024, ContainerState.NEW)
        def fgsContainer4 = getFGSContainer(zeroNM, 4, 1, 1024, ContainerState.NEW)

        when:
        yarnNodeCapacityMgr.handleContainerAllocation(zeroNM)

        then:
        nodeStore.getNode(host).getNode().getRunningContainers().size() == 4  // 2 running + 2 new
        1 * mesosDriver.launchTasks(_ as Collection<Protos.OfferID>, _ as List<Protos.TaskInfo>) // for place holder tasks
        zeroNM.getTotalCapability().getVirtualCores() == 4 // capacity equals size of running + new containers
        zeroNM.getTotalCapability().getMemory() == 4096
        nodeStore.getNode(host).getContainerSnapshot() == null // container snapshot is released
    }

    def "Set Node Capacity"() {
        given:
        def zeroNM = getRMNode(0, 0, "test_host", null)
        def yarnNodeCapacityMgr = getYarnNodeCapacityManager()

        when:
        yarnNodeCapacityMgr.setNodeCapacity(zeroNM, Resources.createResource(2048, 2))

        then:
        zeroNM.getTotalCapability().getMemory() == 2048
        zeroNM.getTotalCapability().getVirtualCores() == 2
        1 * rmContext.getDispatcher().getEventHandler().handle(_ as NodeResourceUpdateSchedulerEvent)
    }

    YarnNodeCapacityManager getYarnNodeCapacityManager() {
        def registry = Mock(InterceptorRegistry)
        def executorInfo = Protos.ExecutorInfo.newBuilder()
                .setExecutorId(Protos.ExecutorID.newBuilder().setValue("some_id"))
                .setCommand(Protos.CommandInfo.newBuilder())
                .build()
        def nodeTask = Mock(NodeTask) {
            getExecutorInfo() >> executorInfo
        }
        def state = Mock(SchedulerState) {
            getNodeTask(_, NodeManagerConfiguration.NM_TASK_PREFIX) >> nodeTask
        }
        def cfg = Mock(MyriadConfiguration) {
            getFrameworkRole() >> "some_role"
        }
        print(cfg.getFrameworkRole())
        def taskUtils = new TaskUtils(cfg)
        return new YarnNodeCapacityManager(registry, yarnScheduler, rmContext,
                myriadDriver, offerLifecycleManager, nodeStore, state, taskUtils)
    }
}
