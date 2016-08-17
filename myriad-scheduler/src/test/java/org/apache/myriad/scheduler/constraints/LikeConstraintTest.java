package org.apache.myriad.scheduler.constraints;

import static org.apache.mesos.Protos.Value.Type.TEXT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.mesos.Protos;
import org.apache.mesos.Protos.Attribute;
import org.apache.mesos.Protos.Value.Text;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
/**
 * Unit tests for LikeConstraint
 */
public class LikeConstraintTest {
  private LikeConstraint constraintOne, constraintTwo, constraintThree;
  
  @Before
  public void setUp() {
    constraintOne = new LikeConstraint("hostname", "host-[0-9]*.example.com");
    constraintTwo = new LikeConstraint("hostname", "host-[0-9]*.example.com");   
    constraintThree = new LikeConstraint("dfs", "dfs-test");
  }

  private Protos.Attribute getTextAttribute(String name, String value) {
    return Protos.Attribute.newBuilder()
      .setName(name)
      .setType(TEXT)
      .setText(Text.newBuilder()
      .setValue(value))
      .build();
  }  
  
  @Test
  public void testEquals() throws Exception {    
    assertTrue(constraintOne.equals(constraintTwo));
    assertFalse(constraintOne.equals(constraintThree));
  }
  
  @Test
  public void testMatchesHostName() throws Exception {
    assertTrue(constraintOne.matchesHostName("host-1.example.com"));
    assertTrue(constraintTwo.matchesHostName("host-1.example.com"));
  }
  
  @Test
  public void testConstraintOnHostame() throws Exception {
    assertTrue(constraintOne.isConstraintOnHostName());
    assertTrue(constraintTwo.isConstraintOnHostName());
    assertFalse(constraintThree.isConstraintOnHostName());
  }
  
  @Test
  public void testGetType() throws Exception {
    assertEquals("LIKE", constraintOne.getType().toString());
    assertEquals("LIKE", constraintTwo.getType().toString());
  }
  
  @Test
  public void testMatchSlaveAttributes() throws Exception {
    List<Attribute> attributes = Lists.newArrayList(getTextAttribute("dfs", "dfs-test"));
    assertTrue(constraintThree.matchesSlaveAttributes(attributes));
  }
}