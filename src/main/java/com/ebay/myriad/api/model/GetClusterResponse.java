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
package com.ebay.myriad.api.model;

import com.ebay.myriad.state.Cluster;

public class GetClusterResponse {
    private Cluster cluster;

    public GetClusterResponse() {
        super();
    }

    public GetClusterResponse(Cluster cluster) {
        super();
        this.cluster = cluster;
    }

    public Cluster getCluster() {
        return cluster;
    }
}
