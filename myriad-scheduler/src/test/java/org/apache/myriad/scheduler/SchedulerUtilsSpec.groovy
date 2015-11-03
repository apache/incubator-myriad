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
package org.apache.myriad.scheduler

import org.apache.mesos.Protos
import org.apache.myriad.configuration.NodeManagerConfiguration
import org.apache.myriad.state.NodeTask
import org.apache.myriad.state.SchedulerState
import spock.lang.Specification

/**
 *
 * @author kensipe
 */
class SchedulerUtilsSpec extends Specification {

    def "is unique host name"() {
        given:
        def offer = Mock(Protos.OfferOrBuilder)
        offer.getHostname() >> "hostname"

        expect:
        returnValue == SchedulerUtils.isUniqueHostname(offer, launchTask, tasks)

        where:
        tasks                                              | launchTask                     | returnValue
        []                                                 | null                           | true
        null                                               | null                           | true
        createNodeTaskList("hostname")                     | createNodeTask("hostname")     | false
        createNodeTaskList("missinghost")                  | createNodeTask("hostname")     | true
        createNodeTaskList("missinghost1", "missinghost2") | createNodeTask("missinghost3") | true
        createNodeTaskList("missinghost1", "hostname")     | createNodeTask("hostname")     | false

    }

    def "is eligible for Fine Grained Scaling"() {
        given:
        def state = Mock(SchedulerState)
        def tasks = []
        def fgsNMTask = new NodeTask(new ExtendedResourceProfile(new NMProfile("zero", 0, 0), 1.0, 2.0), null)
        def cgsNMTask = new NodeTask(new ExtendedResourceProfile(new NMProfile("low", 2, 4096), 1.0, 2.0), null)
        fgsNMTask.setHostname("test_fgs_hostname")
        cgsNMTask.setHostname("test_cgs_hostname")
        tasks << fgsNMTask << cgsNMTask
        state.getActiveTasksByType(NodeManagerConfiguration.NM_TASK_PREFIX) >> tasks

        expect:
        returnValue == SchedulerUtils.isEligibleForFineGrainedScaling(hostName, state)

        where:
        hostName            | returnValue
        "test_fgs_hostname" | true
        "test_cgs_hostname" | false
        "blah"              | false
        ""                  | false
        null                | false
    }

    ArrayList<NodeTask> createNodeTaskList(String... hostnames) {
        def list = []
        hostnames.each { hostname ->
            list << createNodeTask(hostname)
        }
        return list
    }


    NodeTask createNodeTask(String hostname) {
        def node = new NodeTask(new ExtendedResourceProfile(new NMProfile("", 1, 1), 1.0, 1.0), null)
        node.hostname = hostname
        node.taskPrefix = "nm"
        node
    }
}
