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
package org.apache.myriad.scheduler.fgs;

import java.util.Collection;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.Offer;

/**
 * Utility class that provides useful methods that deal with Mesos offers.
 */
public class OfferUtils {

  /**
   * Transforms a collection of mesos offers into {@link Resource}.
   *
   * @param offers collection of mesos offers
   * @return a single resource object equivalent to the cumulative sum of mesos offers
   */
  public static Resource getYarnResourcesFromMesosOffers(Collection<Offer> offers) {
    double cpus = 0.0;
    double mem = 0.0;

    for (Protos.Offer offer : offers) {
      for (Protos.Resource resource : offer.getResourcesList()) {
        if (resource.getName().equalsIgnoreCase("cpus")) {
          cpus += resource.getScalar().getValue();
        } else if (resource.getName().equalsIgnoreCase("mem")) {
          mem += resource.getScalar().getValue();
        }
      }
    }
    return Resource.newInstance((int) mem, (int) cpus);
  }

}
