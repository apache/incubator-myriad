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

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Node Manager Profile Manager
 */
public class NMProfileManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(NMProfileManager.class);

    private Map<String, NMProfile> profiles = new ConcurrentHashMap<>();

    public NMProfile get(String name) {
        return profiles.get(name);
    }

    public void add(NMProfile profile) {
        LOGGER.info("Adding profile {} with CPU: {} and Memory: {}",
                profile.getName(), profile.getCpus(), profile.getMemory());

        profiles.put(profile.getName(), profile);
    }

    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
