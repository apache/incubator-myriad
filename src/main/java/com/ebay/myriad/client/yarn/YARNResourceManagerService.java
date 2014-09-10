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
package com.ebay.myriad.client.yarn;

import retrofit.http.GET;
import retrofit.http.Query;

public interface YARNResourceManagerService {
	@GET("/ws/v1/cluster/appresourcerequests")
	ResourceRequests resourceRequests();

	@GET("/ws/v1/cluster/metrics")
	ClusterMetricsResponse metrics();

	@GET("/ws/v1/cluster/apps")
	AppsResponse apps(@Query("state") String state);
}