package com.ebay.myriad.scheduler;

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
