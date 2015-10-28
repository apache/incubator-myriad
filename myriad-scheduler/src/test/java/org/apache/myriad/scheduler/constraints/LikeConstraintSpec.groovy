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
package org.apache.myriad.scheduler.constraints

import com.google.common.collect.Lists
import org.apache.mesos.Protos
import spock.lang.Specification

import static org.apache.mesos.Protos.Value.Text
import static org.apache.mesos.Protos.Value.Type.TEXT

/**
 *
 * Test for LikeConstraint
 *
 */
class LikeConstraintSpec extends Specification {

    def "is matching host name"() {
        given:
        def constraint = new LikeConstraint("hostname", "host-[0-9]*.example.com")

        expect:
        returnValue == constraint.matchesHostName(inputHostName)

        where:
        inputHostName         | returnValue
        null                  | false
        ""                    | false
        "blah-blue"           | false
        "host-12.example.com" | true
        "host-1.example.com"  | true
        "host-2.example.com"  | true
    }

    def "is matching dfs attribute"() {
        given:
        def constraint = new LikeConstraint("dfs", "true")

        expect:
        returnValue == constraint.matchesSlaveAttributes(attributes)

        where:
        attributes                                                     | returnValue
        null                                                           | false
        Lists.newArrayList()                                           | false
        Lists.newArrayList(getTextAttribute("dfs", ""))                | false
        Lists.newArrayList(getTextAttribute("dfs", "false"))           | false
        Lists.newArrayList(getTextAttribute("Distributed FS", "true")) | false
        Lists.newArrayList(getTextAttribute("dfs", "true"))            | true
        Lists.newArrayList(getTextAttribute("dfs", "true"),
                getTextAttribute("random", "random value"))            | true
    }

    def "equals"() {
        given:
        def constraint1 = new LikeConstraint("hostname", "perfnode13[3-4].perf.lab")
        def constraint2 = new LikeConstraint("hostname", "perfnode13[3-4].perf.lab")
        def constraint3 = new LikeConstraint("hostname", "perfnode133.perf.lab")

        expect:
        constraint1.equals(constraint2)
        !constraint1.equals(constraint3)
        !constraint2.equals(constraint3)
    }

    private static Protos.Attribute getTextAttribute(String name, String value) {
        Protos.Attribute.newBuilder()
                .setName(name)
                .setType(TEXT)
                .setText(Text.newBuilder()
                .setValue(value))
                .build()
    }


}
