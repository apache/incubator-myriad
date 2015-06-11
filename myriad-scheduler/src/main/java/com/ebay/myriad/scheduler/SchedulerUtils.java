/**
 * Copyright 2012-2014 eBay Software Foundation, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.ebay.myriad.scheduler;

import com.ebay.myriad.state.NodeTask;
import com.google.common.base.Preconditions;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.Attribute;
import org.apache.mesos.Protos.Offer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides utilities for scheduling with the mesos offers
 */
public class SchedulerUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerUtils.class);

    public static boolean isMatchSlaveAttributes(Offer offer, Map<String, String> requestAttributes) {
        boolean match = true;

        Map<String, String> offerAttributes = new HashMap<>();
        for (Attribute attribute : offer.getAttributesList()) {
            offerAttributes.put(attribute.getName(), attribute.getText().getValue());
        }

        // Match with offer attributes only if request has attributes.
        if (!MapUtils.isEmpty(requestAttributes)) {
            match = offerAttributes.equals(requestAttributes);
        }

        LOGGER.debug("Match status: {} for offer: {} and requestAttributes: {}",
                match, offer, requestAttributes);

        return match;
    }

    public static boolean isUniqueHostname(Protos.OfferOrBuilder offer,
                                           Collection<NodeTask> tasks) {
        Preconditions.checkArgument(offer != null);
        String offerHostname = offer.getHostname();

        if (CollectionUtils.isEmpty(tasks)) {
            return true;
        }
        boolean uniqueHostname = true;
        for (NodeTask task : tasks) {
            if (offerHostname.equalsIgnoreCase(task.getHostname())) {
                uniqueHostname = false;
            }
        }
        LOGGER.debug("Offer's hostname {} is unique: {}", offerHostname, uniqueHostname);
        return uniqueHostname;
    }
}