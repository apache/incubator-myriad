package org.apache.myriad.scheduler.fgs;

import java.util.List;

import org.apache.mesos.Protos;
import org.apache.mesos.Protos.FrameworkID;
import org.apache.mesos.Protos.Offer;
import org.apache.mesos.Protos.OfferID;
import org.apache.mesos.Protos.Resource;
import org.apache.mesos.Protos.SlaveID;
import org.apache.mesos.Protos.Value;
import org.apache.mesos.Protos.Value.Scalar;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;

/**
 * Unit tests for OfferUtils
 */
public class OfferUtilsTest {

  List<Offer> offers;

  @Before
  public void setUp() throws Exception {
    offers = generateOffers(generateResources());
  }

  private List<Offer> generateOffers(List<Resource> resources) {
    FrameworkID fidOne = Protos.FrameworkID.newBuilder().setValue("framework-1").build();
    FrameworkID fidTwo = Protos.FrameworkID.newBuilder().setValue("framework-2").build();
    FrameworkID fidThree = Protos.FrameworkID.newBuilder().setValue("framework-3").build();
    FrameworkID fidFour = Protos.FrameworkID.newBuilder().setValue("framework-4").build();

    OfferID oidOne = Protos.OfferID.newBuilder().setValue("offer-1").build();
    OfferID oidTwo = Protos.OfferID.newBuilder().setValue("offer-2").build(); 
    OfferID oidThree = Protos.OfferID.newBuilder().setValue("offer-3").build(); 
    OfferID oidFour = Protos.OfferID.newBuilder().setValue("offer-4").build(); 

    SlaveID sidOne = Protos.SlaveID.newBuilder().setValue("slave-1").build();
    SlaveID sidTwo = Protos.SlaveID.newBuilder().setValue("slave-2").build();
    SlaveID sidThree = Protos.SlaveID.newBuilder().setValue("slave-3").build();
    SlaveID sidFour = Protos.SlaveID.newBuilder().setValue("slave-4").build();

    Offer offerOne = Protos.Offer.newBuilder().setFrameworkId(fidOne).setHostname("10.0.0.1").setId(oidOne).setSlaveId(sidOne).
        addResources(resources.get(0)).addResources(resources.get(1)).build();
    Offer offerTwo = Protos.Offer.newBuilder().setFrameworkId(fidTwo).setHostname("10.0.0.2").setId(oidTwo).setSlaveId(sidTwo).
        addResources(resources.get(2)).addResources(resources.get(3)).build();
    Offer offerThree = Protos.Offer.newBuilder().setFrameworkId(fidThree).setHostname("10.0.0.3").setId(oidThree).setSlaveId(sidThree).
        addResources(resources.get(0)).addResources(resources.get(3)).build();
    Offer offerFour = Protos.Offer.newBuilder().setFrameworkId(fidFour).setHostname("10.0.0.4").setId(oidFour).setSlaveId(sidFour).
        addResources(resources.get(2)).addResources(resources.get(1)).build();

    return Lists.newArrayList(offerOne, offerTwo, offerThree, offerFour);
  }
  
  private List<Resource> generateResources() {
    Resource rOne = Protos.Resource.newBuilder().setName("cpus").setType(Value.Type.SCALAR).setScalar(Scalar.newBuilder().setValue(0.5)).build();
    Resource rTwo = Protos.Resource.newBuilder().setName("mem").setType(Value.Type.SCALAR).setScalar(Scalar.newBuilder().setValue(1.0)).build();
    Resource rThree = Protos.Resource.newBuilder().setName("cpus").setType(Value.Type.SCALAR).setScalar(Scalar.newBuilder().setValue(1.0)).build();
    Resource rFour = Protos.Resource.newBuilder().setName("mem").setType(Value.Type.SCALAR).setScalar(Scalar.newBuilder().setValue(2.0)).build();
    
    return Lists.newArrayList(rOne, rTwo, rThree, rFour);
  }
  
  @Test
  public void testgetYarnResourcesFromMesosOffers() throws Exception {
    org.apache.hadoop.yarn.api.records.Resource resource = OfferUtils.getYarnResourceFromMesosOffers(offers);
    assertEquals(6.0, resource.getMemory(), 1.0);
    assertEquals(3.0, resource.getVirtualCores(), 1.0);
  }
}