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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.apache.hadoop.yarn.api.records.*
import org.apache.hadoop.yarn.conf.YarnConfiguration
import org.apache.hadoop.yarn.event.Dispatcher
import org.apache.hadoop.yarn.event.EventHandler
import org.apache.hadoop.yarn.server.resourcemanager.MockNodes
import org.apache.hadoop.yarn.server.resourcemanager.RMContext
import org.apache.hadoop.yarn.server.resourcemanager.ahs.RMApplicationHistoryWriter
import org.apache.hadoop.yarn.server.resourcemanager.metrics.SystemMetricsPublisher
import org.apache.hadoop.yarn.server.resourcemanager.rmcontainer.RMContainer
import org.apache.hadoop.yarn.server.resourcemanager.rmcontainer.RMContainerImpl
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNode
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.AbstractYarnScheduler
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.SchedulerApplicationAttempt
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.SchedulerNode
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.fair.FSSchedulerNode
import org.apache.hadoop.yarn.util.resource.Resources
import org.apache.mesos.Protos
import org.apache.mesos.SchedulerDriver
import org.apache.myriad.configuration.MyriadConfiguration
import org.apache.myriad.scheduler.MyriadDriver
import spock.lang.Specification

import java.util.concurrent.ConcurrentHashMap

/**
 *
 * Base class for testing Fine Grained Scaling.
 *
 */
class FGSTestBaseSpec extends Specification {
    def nodeStore = new NodeStore()
    def mesosDriver = Mock(SchedulerDriver)
    def myriadDriver = new MyriadDriver(mesosDriver)
    def offerLifecycleManager = new OfferLifecycleManager(nodeStore, myriadDriver)

    def cfg = new MyriadConfiguration()

    void setup() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
        cfg = mapper.readValue(
                Thread.currentThread().getContextClassLoader().getResource("myriad-config-default.yml"),
                MyriadConfiguration.class)
    }
/******************* Nodes Related ****************/

    def rmNodes = new ConcurrentHashMap<NodeId, RMNode>()


    RMNode getRMNode(int cpu, int mem, String host, Protos.SlaveID slaveId) {
        RMNode rmNode = MockNodes.newNodeInfo(0, Resources.createResource(mem, cpu), 0, host)
        if (rmNodes[rmNode.getNodeID()]) {
            throw new IllegalArgumentException("Node with hostname: " + host + " already exists")
        }
        rmNodes.put(rmNode.getNodeID(), rmNode)
        nodeStore.add(getSchedulerNode(rmNode))
        def node = nodeStore.getNode(host)
        node.setSlaveId(slaveId)

        return rmNode
    }

    SchedulerNode getSchedulerNode(RMNode rmNode) {
        SchedulerNode schedulerNode = new SchedulerNode(rmNode, false) {
            @Override
            void reserveResource(SchedulerApplicationAttempt attempt, Priority priority, RMContainer container) {
            }
            @Override
            void unreserveResource(SchedulerApplicationAttempt attempt) {
            }
        }
        return schedulerNode
    }


    /******************* RMContext Related ****************/

    def publisher = Mock(SystemMetricsPublisher) {}
    def writer = Mock(RMApplicationHistoryWriter) {}
    def handler = Mock(EventHandler) {}

    def dispatcher = Mock(Dispatcher) {
        getEventHandler() >> handler
    }

    def rmContext = Mock(RMContext) {
        getDispatcher() >> dispatcher
        getRMApplicationHistoryWriter() >> writer
        getSystemMetricsPublisher() >> publisher
        getRMNodes() >> rmNodes
        getYarnConfiguration() >> new YarnConfiguration()
    }

    /******************* Offers Related ****************/

    Protos.Offer addOfferToFeed(Protos.SlaveID slaveID, String host, int cpu, int mem) {
        def offer = Protos.Offer.newBuilder()
                .setId(Protos.OfferID.newBuilder().setValue("test_offer_id"))
                .setFrameworkId(Protos.FrameworkID.newBuilder().setValue("test_framework_id"))
                .setSlaveId(slaveID)
                .setHostname(host)
                .addResources(Protos.Resource.newBuilder()
                .setName("cpus")
                .setScalar(Protos.Value.Scalar.newBuilder().setValue(cpu))
                .setType(Protos.Value.Type.SCALAR).build())
                .addResources(Protos.Resource.newBuilder()
                .setName("mem")
                .setScalar(Protos.Value.Scalar.newBuilder().setValue(mem))
                .setType(Protos.Value.Type.SCALAR).build())
                .build()
        offerLifecycleManager.addOffers(offer)
        return offer
    }

    /******************* Containers Related ****************/

    class FGSContainer {
        ContainerId containerId
        Container container
        RMContainer rmContainer
        ContainerStatus containerStatus
    }

    def fgsContainers = new HashMap<>()

    AbstractYarnScheduler yarnScheduler = Mock(AbstractYarnScheduler) {
        getRMContainer(_ as ContainerId) >> { ContainerId cid -> fgsContainers.get(cid).rmContainer }
        getSchedulerNode(_ as NodeId) >> { NodeId nodeId -> getSchedulerNode(rmNodes.get(nodeId)) }
        updateNodeResource(_ as RMNode, _ as ResourceOption) >> { }
    }

    FGSContainer getFGSContainer(RMNode node, int cid, int cpu, int mem, ContainerState state) {
        FGSContainer fgsContainer = createFGSContainer(node, cid, cpu, mem, state)
        if (!fgsContainers[fgsContainer.containerId]) {
            fgsContainers[fgsContainer.containerId] = fgsContainer
        }
        return fgsContainer
    }

    private FGSContainer createFGSContainer(RMNode node, int cid, int cpu, int mem, ContainerState state) {
        ContainerId containerId = ContainerId.newContainerId(ApplicationAttemptId.newInstance(
                ApplicationId.newInstance(123456789, 1), 1), cid)
        FGSContainer fgsContainer = new FGSContainer()
        fgsContainer.containerId = containerId
        fgsContainer.container = Container.newInstance(containerId, node.getNodeID(), node.getHttpAddress(),
                Resources.createResource(mem, cpu), null, null)
        fgsContainer.rmContainer = new RMContainerImpl(fgsContainer.container, containerId.getApplicationAttemptId(),
                node.getNodeID(), "user1", rmContext)
        nodeStore.getNode(node.getNodeID().getHost()).getNode().allocateContainer(fgsContainer.rmContainer)
        fgsContainer.containerStatus = ContainerStatus.newInstance(containerId, state, "", 0)
        return fgsContainer
    }

}
