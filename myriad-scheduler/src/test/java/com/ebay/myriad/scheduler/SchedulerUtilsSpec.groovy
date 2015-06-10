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
        returnValue == SchedulerUtils.isUniqueHostname(offer, tasks)

        where:
        tasks                                              | returnValue
        []                                                 | true
        null                                               | true
        createNodeTaskList("hostname")                     | false
        createNodeTaskList("missinghost")                  | true
        createNodeTaskList("missinghost1", "missinghost2") | true
        createNodeTaskList("missinghost1", "hostname")     | false

    }

    ArrayList<NodeTask> createNodeTaskList(String... hostnames) {
        def list = []
        hostnames.each { hostname ->
            list << createNodeTask(hostname)
        }
        return list
    }


    NodeTask createNodeTask(String hostname) {
        def node = new NodeTask(new NMProfile("", 1, 1))
        node.hostname = hostname
        node
    }
}
