/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myriad.scheduler.resource;

import com.google.common.base.Preconditions;
import org.apache.mesos.Protos;
import org.apache.myriad.scheduler.ServiceResourceProfile;
import org.apache.myriad.scheduler.constraints.Constraint;
import org.apache.myriad.scheduler.constraints.LikeConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Container class to get and keep track of mesos resources
 */
public class ResourceOfferContainer {
  private static final Logger LOGGER = LoggerFactory.getLogger(ResourceOfferContainer.class);
  private static final String RESOURCE_CPUS = "cpus";
  private static final String RESOURCE_MEM = "mem";
  private static final String RESOURCE_PORTS = "ports";

  private HashMap<String, ScalarResource> scalarValues = new HashMap<>();
  private HashMap<String, RangeResource> rangeValues = new HashMap<>();

  private Protos.Offer offer;
  private String role;

  /**
   * Constructor takes an offer and profile and constructs a mutable POJO to handle resource offers.
   *
   * @param offer   Mesos.Protos.Offer
   * @param profile ServiceResourceProfile
   */
  public ResourceOfferContainer(Protos.Offer offer, ServiceResourceProfile profile, String role) {
    this.offer = offer;
    this.role = role;
    setScalarValues();
    //ports = new RangeResource(offer, RESOURCE_PORTS, profile.getPorts().values(), role);
  }

  /**
   * returns the hostname contained in the offer
   *
   * @return hostname
   */
  public String getHostName() {
    return offer.getHostname();
  }

  public String getOfferId() {
    return offer.getId().getValue();
  }

  public Protos.SlaveID getSlaveId() {
    return offer.getSlaveId();
  }

  public double getScalarValue(String name) {
    return scalarValues.get(name).getTotalValue();
  }

  public double getCpus() {
    return getScalarValue(RESOURCE_CPUS);
  }

  public double getMem() {
    return getScalarValue(RESOURCE_MEM);
  }

  public List<Long> getPorts() {
    return rangeValues.get(RESOURCE_PORTS).getValues();
  }

  /**
   * Returns true if the offer meets the profile resource needs
   *
   * @param profile
   * @return
   */
  public boolean satisfies(ServiceResourceProfile profile) {
    return scalarValues.containsKey(RESOURCE_CPUS) && scalarValues.get(RESOURCE_CPUS).satisfies(profile.getAggregateCpu()) &&
        scalarValues.containsKey(RESOURCE_MEM) && scalarValues.get(RESOURCE_MEM).satisfies(profile.getAggregateMemory()) &&
        rangeValues.containsKey(RESOURCE_PORTS) && rangeValues.get(RESOURCE_PORTS).satisfies(profile.getPorts().values());
  }

  /**
   * Returns true if offer meets the profile resource needs AND the task constaint (an attibritute of hostname)
   *
   * @param profile
   * @param constraint
   * @return
   */
  public boolean satisfies(ServiceResourceProfile profile, Constraint constraint) {
    return satisfies(profile) && meetsConstraint(constraint);
  }

  private boolean meetsConstraint(Constraint constraint) {
    if (constraint != null) {
      switch (constraint.getType()) {
        case LIKE: {
          LikeConstraint likeConstraint = (LikeConstraint) constraint;
          if (likeConstraint.isConstraintOnHostName()) {
            return likeConstraint.matchesHostName(offer.getHostname());
          } else {
            return likeConstraint.matchesSlaveAttributes(offer.getAttributesList());
          }
        }
        default:
          return false;
      }
    }
    return true;
  }

  private List<Protos.Resource> consumeScalarResource(String name, Double value) {
    Preconditions.checkState(scalarValues.containsKey(name));
    return scalarValues.get(name).consumeResource(value);
  }

  /**
   * Returns a list of CPU Resources meeting the requested value.
   * Decrements the available CPU resources available in the offer.
   * Uses Preconditions the ensure value is not more that the amount the offer has.
   *
   * @param value
   * @return List<Protos.Resource>
   */
  public List<Protos.Resource> consumeCpus(Double value) {
    return consumeScalarResource(RESOURCE_CPUS, value);
  }

  /**
   * Returns a list of MEM Resources meeting the requested value.
   * Decrements the available MEM resources available in the offer.
   * Uses Preconditions the ensure value is not more that the amount the offer has.
   *
   * @param value
   * @return List<Protos.Resource>
   */
  public List<Protos.Resource> consumeMem(Double value) {
    return consumeScalarResource(RESOURCE_MEM, value);
  }

  /**
   * Returns a list of Range Resources meeting the requestedvalues.
   * Removes the requested values from the available range resources available in the offer.
   * Uses Preconditions the ensure values are contained in the offer.
   *
   * @param requestedValues
   * @return List<Protos.Resource>
   */
  public List<Protos.Resource> consumePorts(Collection<Long> requestedValues) {
    return rangeValues.get(RESOURCE_PORTS).consumeResource(requestedValues);
  }

  private void setScalarValues() {
    for (Protos.Resource r : offer.getResourcesList()) {
      if (r.hasScalar() && r.hasName() && r.hasRole() && r.getRole().equals(role)) {
        addToScalarResource(r.getName(), r.getScalar().getValue(), true);
      } else if (r.hasName() && r.hasScalar()) {
        addToScalarResource(r.getName(), r.getScalar().getValue(), false);
      } else if (r.hasRanges() && r.hasName() && r.hasRole() && r.getRole().equals(role)) {
        addToRangeResource(r.getName(), r.getRanges().getRangeList(), true);
      } else if (r.hasRanges() && r.hasName()) {
        addToRangeResource(r.getName(), r.getRanges().getRangeList(), false);
      }
    }
  }

  private void addToScalarResource(String name, Double value, Boolean hasRole) {
    if (scalarValues.containsKey(name)) {
      scalarValues.get(name).incrementValue(value, hasRole);
    } else {
      scalarValues.put(name, new ScalarResource(name, role));
      scalarValues.get(name).incrementValue(value, hasRole);
    }
  }

  private void addToRangeResource(String name, List<Protos.Value.Range> values , Boolean hasRole) {
    if (rangeValues.containsKey(name)) {
      rangeValues.get(name).addRanges(values, hasRole);
    } else {
      rangeValues.put(name, new RangeResource(name, role));
      rangeValues.get(name).addRanges(values, hasRole);
    }
  }


}
