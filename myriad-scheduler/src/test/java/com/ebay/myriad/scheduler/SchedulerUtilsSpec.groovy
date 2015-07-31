package com.ebay.myriad.scheduler

import com.ebay.myriad.state.NodeTask
import org.apache.mesos.Protos
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
        tasks                                              | launchTask 					| returnValue
        []                                                 | null							| true
        null                                               | null							| true
        createNodeTaskList("hostname")                     | createNodeTask("hostname") 	| false
        createNodeTaskList("missinghost")                  | createNodeTask("hostname") 	| true
        createNodeTaskList("missinghost1", "missinghost2") | createNodeTask("missinghost3")	| true
        createNodeTaskList("missinghost1", "hostname")     | createNodeTask("hostname")		| false

    }

    ArrayList<NodeTask> createNodeTaskList(String... hostnames) {
        def list = []
        hostnames.each { hostname ->
            list << createNodeTask(hostname)
        }
        return list
    }


    NodeTask createNodeTask(String hostname) {
        def node = new NodeTask(new ExtendedResourceProfile(new NMProfile("", 1, 1), 1.0,1.0), null)
        node.hostname = hostname
        node.taskPrefix = "nm"
        node
    }
}
