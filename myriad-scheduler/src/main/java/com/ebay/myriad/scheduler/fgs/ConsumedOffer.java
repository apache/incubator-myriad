package com.ebay.myriad.scheduler.fgs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.mesos.Protos;

/**
 * Represents offers from a slave that have been consumed by Myriad.
 */
public class ConsumedOffer {
  private List<Protos.Offer> offers;

  public ConsumedOffer() {
    this.offers = new LinkedList<>();
  }

  public void add(Protos.Offer offer) {
    offers.add(offer);
  }

  public List<Protos.Offer> getOffers() {
    return offers;
  }

  public Collection<Protos.OfferID> getOfferIds() {
    Collection<Protos.OfferID> ids = new ArrayList<>(offers.size());

    for (Protos.Offer offer : offers) {
      ids.add(offer.getId());
    }

    return ids;
  }
}
