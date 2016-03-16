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
package org.apache.myriad.scheduler;

import org.apache.hadoop.yarn.api.records.Resource;
/**
 * Small class of Yarn resource utils.  Some methods may be redundant with methods in
 * org.apache.hadoop.yarn.util.resource.Resources as of 2.7.0 but are here for backwards compatibilty
 * with 2.6.0
 */
public class ResourceUtils {
  public static Resource componentwiseMax(Resource lhs, Resource rhs) {
    int cores = Math.max(lhs.getVirtualCores(), rhs.getVirtualCores());
    int mem = Math.max(lhs.getMemory(), rhs.getMemory());
    return Resource.newInstance(cores, mem);
  }
}