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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.myriad.state.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.*;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.CommandInfo;
import org.apache.mesos.Protos.ExecutorID;
import org.apache.mesos.Protos.ExecutorInfo;
import org.apache.mesos.Protos.FrameworkID;
import org.apache.myriad.scheduler.ServiceResourceProfile;
import org.apache.myriad.scheduler.constraints.LikeConstraint;
import org.apache.myriad.state.NodeTask;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

/**
 * Unit tests for ByteBufferSupport class
 *
 */
public class ByteBufferSupportTest {

  private static final byte[] BYTE_ARRAY = getByteArray("e04fd020ea3a6910a2d808002b30309d");

  private NodeTask task;

  @Before
  public void setUp() throws Exception {
    task = new NodeTask(new ServiceResourceProfile("profile", 0.1, 1024.0, new TreeMap<String, Long>()),
        new LikeConstraint("hostname", "host-[0-9]*.example.com"));
    task.setHostname("localhost");
    task.setTaskPrefix("prefix"); 
    task.setExecutorInfo(getExecutorInfo());
  }

  private ExecutorInfo getExecutorInfo() {
    FrameworkID id = Protos.FrameworkID.newBuilder().setValue("framework1").build();
    ExecutorID eid = Protos.ExecutorID.newBuilder().setValue("executor1").build();
    CommandInfo cm = Protos.CommandInfo.newBuilder().setValue("command").build();
    return ExecutorInfo.newBuilder().setFrameworkId(id).setExecutorId(eid).setCommand(cm).build();
  }

  private ByteBuffer getByteBuffer(byte[] bytes) {
    ByteBuffer bb = ByteBuffer.allocate(bytes.length);
    bb.put(bytes);
    bb.rewind();  
    return bb;
  }

  private static byte[] getByteArray(String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) 
        + Character.digit(s.charAt(i + 1), 16));
    }
    return data;
  }

  @Test
  public void testGetBytes() throws Exception {
    ByteBuffer bb = getByteBuffer(BYTE_ARRAY);
    
    byte[] bytes = ByteBufferSupport.getBytes(bb, bb.capacity());
    
    assertEquals(BYTE_ARRAY.length, bytes.length);
    
    for (int i = 0, j = bytes.length; i < j; i++) {
      assertEquals(bytes[i], BYTE_ARRAY[i]);
    }
  }
  
  @Test
  public void testFillBuffer() throws Exception {
    ByteBuffer bb = ByteBufferSupport.fillBuffer(BYTE_ARRAY);
    ByteBuffer bbCompare = getByteBuffer(BYTE_ARRAY);

    assertEquals(bb, bbCompare); 
  }
  
  @Test
  public void testNonEmptyAddByteBuffer() throws Exception {
    ByteBuffer bb = getByteBuffer(BYTE_ARRAY);
    ByteArrayOutputStream stream = new ByteArrayOutputStream();

    ByteBufferSupport.addByteBuffer(bb, stream);
    assertEquals(20, stream.size());
    ByteBufferSupport.addByteBuffer(bb, stream);
    assertEquals(40, stream.size());
  }
  
  @Test
  public void testEmptyAddByteBuffer() throws Exception {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    ByteBuffer bb = getByteBuffer(ArrayUtils.EMPTY_BYTE_ARRAY);
    ByteBufferSupport.addByteBuffer(bb, stream);
    assertEquals(0, stream.size());
  }  
  
  @Test
  public void testNullAddByteBuffer() throws Exception {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();

    ByteBufferSupport.addByteBuffer(null, stream);
    assertEquals(0, stream.size());
  }
  
  @Test
  public void testNonEmptyAddByteBufferList() throws Exception {
    ByteBuffer bb = getByteBuffer(BYTE_ARRAY);
    ByteBuffer bbTwo = getByteBuffer(BYTE_ARRAY);
    ByteArrayOutputStream stream = new ByteArrayOutputStream();

    List<ByteBuffer> bList = Lists.newArrayList(bb, bbTwo);
    
    ByteBufferSupport.addByteBuffers(bList, stream);
    assertEquals(44, stream.size());
  }

  @Test
  public void testToIntBuffer() throws Exception {
    byte[] bytes = ByteBufferSupport.toIntBytes(10);
    assertEquals(4, bytes.length);
    assertEquals(10, bytes[0]);
    assertEquals(0, bytes[1]);
    assertEquals(0, bytes[2]);
    assertEquals(0, bytes[3]);
  }

  @Test
  public void testNodeTaskToFromByteBuffer() throws Exception {
    ByteBuffer bb = ByteBufferSupport.toByteBuffer(task);
    NodeTask sTask = ByteBufferSupport.toNodeTask(bb);

    assertEquals(task.getClass().getName(), sTask.getClass().getName());
    assertEquals(task.getHostname(), sTask.getHostname());
    assertEquals(task.getSlaveId(), sTask.getSlaveId());
    assertEquals(task.getTaskPrefix(), sTask.getTaskPrefix());
    assertEquals(task.getProfile(), sTask.getProfile());
    assertEquals(task.getSlaveAttributes(), sTask.getSlaveAttributes());
    assertEquals(task.getConstraint(), sTask.getConstraint());
    assertEquals(task.getExecutorInfo(), sTask.getExecutorInfo());
  }

  @Test
  public void testFrameworkIDToFromByteBuffer() throws Exception {
    ByteBuffer bb = ByteBufferSupport.toByteBuffer(getExecutorInfo().getFrameworkId());
    FrameworkID id = ByteBufferSupport.toFrameworkID(bb);

    assertEquals(getExecutorInfo().getFrameworkId(), id);
  }

  @Test
  public void testToString() throws Exception {
    String output = ByteBufferSupport.toString(ByteBufferSupport.toByteBuffer(task));

    assertTrue(output.contains("\"name\":\"profile\""));
    assertTrue(output.contains("\"cpus\":0.1"));
    assertTrue(output.contains("\"memory\":1024.0"));
    assertTrue(output.contains("\"executorCpu\":0.0"));
    assertTrue(output.contains("\"executorMemory\":0.0"));
    assertTrue(output.contains("\"className\":\"org.apache.myriad.scheduler.ServiceResourceProfile\""));
  }

  public void testExecutorInfoToFromByteBuffer() throws Exception {
    ExecutorInfo info = getExecutorInfo();   
    ByteBuffer bb = ByteBufferSupport.toByteBuffer(task);
    ExecutorInfo bInfo = ByteBufferSupport.toExecutorInfo(bb);

    assertEquals(info.getClass().getName(), bInfo.getClass().getName());
  }

  public void testGetConstraint() throws Exception {
    ByteBuffer bb = ByteBufferSupport.toByteBuffer(task);

    assertEquals(task.getConstraint(), ByteBufferSupport.getConstraint(bb));
  }
}