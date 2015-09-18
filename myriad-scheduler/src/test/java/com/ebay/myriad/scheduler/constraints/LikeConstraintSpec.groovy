package com.ebay.myriad.scheduler.constraints

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
        getTextAttribute("random", "random value"))                | true
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
