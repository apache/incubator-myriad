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

import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.mesos.Protos;

/**
 * Feed of Mesos offers for a node.
 */
public class OfferFeed {
  private ConcurrentLinkedQueue<Protos.Offer> queue;

  public OfferFeed() {
    this.queue = new ConcurrentLinkedQueue<>();
  }

  public void add(Protos.Offer offer) {
    queue.add(offer);
  }

  /**
   * Retrieves and removes the head of the feed, or returns NULL if the feed is
   * empty.
   */
  public Protos.Offer poll() {
    return queue.poll();
  }
}
