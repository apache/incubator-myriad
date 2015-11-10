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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.Offer;
import org.apache.myriad.scheduler.MyriadDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the Mesos offers tracked by Myriad.
 */
public class OfferLifecycleManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(OfferLifecycleManager.class);

  private Map<String, OfferFeed> offerFeedMap;

  /**
   * !!! Not thread safe !!!
   */
  private final Map<String, ConsumedOffer> consumedOfferMap;

  private final NodeStore nodeStore;
  private final MyriadDriver myriadDriver;

  @Inject
  public OfferLifecycleManager(NodeStore nodeStore, MyriadDriver myriadDriver) {

    this.offerFeedMap = new ConcurrentHashMap<>(200, 0.75f, 50);
    this.consumedOfferMap = new HashMap<>(200, 0.75f);
    this.nodeStore = nodeStore;
    this.myriadDriver = myriadDriver;
  }

  public OfferFeed getOfferFeed(String hostname) {
    return offerFeedMap.get(hostname);
  }

  public void declineOffer(Protos.Offer offer) {
    myriadDriver.getDriver().declineOffer(offer.getId());
    LOGGER.debug("Declined offer {}", offer.getId());
  }

  public void addOffers(Protos.Offer... offers) {
    for (Protos.Offer offer : offers) {
      String hostname = offer.getHostname();
      Node node = nodeStore.getNode(hostname);
      if (node != null) {
        OfferFeed feed = offerFeedMap.get(hostname);
        if (feed == null) {
          feed = new OfferFeed();
          offerFeedMap.put(hostname, feed);
        }
        feed.add(offer);

        node.setSlaveId(offer.getSlaveId());

        LOGGER.debug("addResourceOffers: caching offer for host {}, offer id {}", hostname, offer.getId().getValue());
      } else {
        myriadDriver.getDriver().declineOffer(offer.getId());
        LOGGER.debug("Declined offer for unregistered host {}", hostname);
      }
    }
  }

  public void markAsConsumed(Protos.Offer offer) {
    ConsumedOffer consumedOffer = consumedOfferMap.get(offer.getHostname());
    if (consumedOffer == null) {
      consumedOffer = new ConsumedOffer();
      consumedOfferMap.put(offer.getHostname(), consumedOffer);
    }

    consumedOffer.add(offer);
  }

  public ConsumedOffer drainConsumedOffer(String hostname) {
    return consumedOfferMap.remove(hostname);
  }

  public void declineOutstandingOffers(String hostname) {
    int numOutStandingOffers = 0;
    OfferFeed offerFeed = getOfferFeed(hostname);
    Offer offer;
    while (offerFeed != null && (offer = offerFeed.poll()) != null) {
      declineOffer(offer);
      numOutStandingOffers++;
    }
    if (numOutStandingOffers > 0) {
      LOGGER.info("Declined {} outstanding offers for host {}", numOutStandingOffers, hostname);
    }
  }
}
