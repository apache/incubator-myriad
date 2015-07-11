package com.ebay.myriad.scheduler.fgs;

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
