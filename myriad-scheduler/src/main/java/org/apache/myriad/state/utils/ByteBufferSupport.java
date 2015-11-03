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

package org.apache.myriad.state.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.GeneratedMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.mesos.Protos;
import org.apache.myriad.scheduler.ServiceResourceProfile;
import org.apache.myriad.scheduler.constraints.Constraint;
import org.apache.myriad.scheduler.constraints.Constraint.Type;
import org.apache.myriad.scheduler.constraints.LikeConstraint;
import org.apache.myriad.state.NodeTask;

/**
 * ByteBuffer support for the Serialization of the StoreContext
 */
public class ByteBufferSupport {

  public static final int INT_SIZE = Integer.SIZE / Byte.SIZE;
  public static final String UTF8 = "UTF-8";
  public static final byte[] ZERO_BYTES = new byte[0];
  private static Gson gson = new Gson();
  private static Gson gsonCustom = new GsonBuilder().registerTypeAdapter(ServiceResourceProfile.class,
      new ServiceResourceProfile.CustomDeserializer()).create();

  public static void addByteBuffers(List<ByteBuffer> list, ByteArrayOutputStream bytes) throws IOException {
    // If list, add the list size, then the size of each buffer followed by the buffer.
    if (list != null) {
      bytes.write(toIntBytes(list.size()));
      for (ByteBuffer bb : list) {
        addByteBuffer(bb, bytes);
      }
    } else {
      bytes.write(toIntBytes(0));
    }
  }

  public static void addByteBuffer(ByteBuffer bb, ByteArrayOutputStream bytes) throws IOException {
    if (bb != null && bytes != null) {
      bytes.write(toIntBytes(bb.array().length));
      bytes.write(bb.array());
    }
  }

  public static ByteBuffer toByteBuffer(Protos.TaskID taskId) {
    return toBuffer(taskId);
  }

  public static ByteBuffer toByteBuffer(Protos.FrameworkID frameworkId) {
    return toBuffer(frameworkId);
  }

  /*
   * Common method to convert Protobuf object to ByteBuffer 
   */
  public static ByteBuffer toBuffer(GeneratedMessage message) {
    byte dst[];
    int size;
    if (message != null) {
      size = message.getSerializedSize() + INT_SIZE;
      dst = message.toByteArray();
    } else {
      size = INT_SIZE;
      dst = ZERO_BYTES;
    }
    ByteBuffer bb = createBuffer(size);
    putBytes(bb, dst);
    bb.rewind();
    return bb;
  }

  public static byte[] toIntBytes(int src) {
    ByteBuffer bb = createBuffer(INT_SIZE);
    bb.putInt(src);
    return bb.array();
  }


  public static ByteBuffer toByteBuffer(NodeTask nt) {
    // Determine the size of ByteBuffer to allocate
    // The ServiceResourceProfile toString() returns Json, if this ever changes then this
    // will fail. Json is expected.
    byte[] profile = toBytes(nt.getProfile().toString());
    int size = profile.length + INT_SIZE;

    Constraint constraint = nt.getConstraint();
    Constraint.Type type = constraint == null ? Type.NULL : constraint.getType();
    size += INT_SIZE;

    byte[] constraintBytes = ZERO_BYTES;
    if (constraint != null) {
      constraintBytes = toBytes(constraint.toString());
      size += constraintBytes.length + INT_SIZE;
    } else {
      size += INT_SIZE;
    }

    byte[] hostname = toBytes(nt.getHostname());
    size += hostname.length + INT_SIZE;

    if (nt.getSlaveId() != null) {
      size += nt.getSlaveId().getSerializedSize() + INT_SIZE;
    } else {
      size += INT_SIZE;
    }

    if (nt.getTaskStatus() != null) {
      size += nt.getTaskStatus().getSerializedSize() + INT_SIZE;
    } else {
      size += INT_SIZE;
    }

    if (nt.getExecutorInfo() != null) {
      size += nt.getExecutorInfo().getSerializedSize() + INT_SIZE;
    } else {
      size += INT_SIZE;
    }

    byte[] taskPrefixBytes = ZERO_BYTES;
    if (nt.getTaskPrefix() != null) {
      taskPrefixBytes = toBytes(nt.getTaskPrefix());
      size += taskPrefixBytes.length + INT_SIZE;
    }

    // Allocate and populate the buffer.
    ByteBuffer bb = createBuffer(size);
    putBytes(bb, profile);
    bb.putInt(type.ordinal());
    putBytes(bb, constraintBytes);
    putBytes(bb, hostname);
    putBytes(bb, getSlaveBytes(nt));
    putBytes(bb, getTaskBytes(nt));
    putBytes(bb, getExecutorInfoBytes(nt));
    putBytes(bb, taskPrefixBytes);
    // Make sure the buffer is at the beginning
    bb.rewind();
    return bb;
  }

  /**
   * Assumes the entire ByteBuffer is a TaskID.
   *
   * @param bb
   * @return Protos.TaskID
   */
  public static Protos.TaskID toTaskId(ByteBuffer bb) {
    try {
      return Protos.TaskID.parseFrom(getBytes(bb, bb.getInt()));
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse Task ID", e);
    }
  }

  /**
   * Assumes the entire ByteBuffer is a FrameworkID.
   *
   * @param bb
   * @return Protos.FrameworkID
   */
  public static Protos.FrameworkID toFrameworkID(ByteBuffer bb) {
    try {
      return Protos.FrameworkID.parseFrom(getBytes(bb, bb.getInt()));
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse Framework ID", e);
    }
  }

  /**
   * ByteBuffer is expected to have a NodeTask at its next position.
   *
   * @param bb
   * @return NodeTask or null if buffer is empty. Can throw a RuntimeException
   * if the buffer is not formatted correctly.
   */
  public static NodeTask toNodeTask(ByteBuffer bb) {
    NodeTask nt = null;
    if (bb != null && bb.array().length > 0) {
      nt = new NodeTask(getServiceResourceProfile(bb), getConstraint(bb));
      nt.setHostname(toString(bb));
      nt.setSlaveId(toSlaveId(bb));
      nt.setTaskStatus(toTaskStatus(bb));
      nt.setExecutorInfo(toExecutorInfo(bb));
    }
    return nt;
  }

  public static byte[] getTaskBytes(NodeTask nt) {
    if (nt.getTaskStatus() != null) {
      return nt.getTaskStatus().toByteArray();
    } else {
      return ZERO_BYTES;
    }
  }

  public static byte[] getExecutorInfoBytes(NodeTask nt) {
    if (nt.getExecutorInfo() != null) {
      return nt.getExecutorInfo().toByteArray();
    } else {
      return ZERO_BYTES;
    }
  }

  public static byte[] getSlaveBytes(NodeTask nt) {
    if (nt.getSlaveId() != null) {
      return nt.getSlaveId().toByteArray();
    } else {
      return ZERO_BYTES;
    }
  }

  public static void putBytes(ByteBuffer bb, byte bytes[]) {
    if (bytes != null && bytes.length > 0) {
      bb.putInt(bytes.length);
      bb.put(bytes);
    } else {
      bb.putInt(0);
    }
  }

  public static byte[] getBytes(ByteBuffer bb, int size) {
    byte bytes[] = new byte[size];
    bb.get(bytes);
    return bytes;
  }

  /**
   * This assumes the next position is the size as an int, and the following is a string
   * iff the size is not zero.
   *
   * @param bb ByteBuffer to extract string from
   * @return string from the next position, or "" if the size is zero
   */
  public static String toString(ByteBuffer bb) {
    byte[] bytes = new byte[bb.getInt()];
    String s = "";
    try {
      if (bytes.length > 0) {
        bb.get(bytes);
        s = new String(bytes, UTF8);
      }
    } catch (Exception e) {
      throw new RuntimeException("ByteBuffer not in expected format," + " failed to parse string bytes", e);
    }
    return s;
  }

  public static byte[] toBytes(String s) {
    try {
      return s.getBytes(UTF8);
    } catch (Exception e) {
      return ZERO_BYTES;
    }
  }

  public static ServiceResourceProfile getServiceResourceProfile(ByteBuffer bb) {
    String p = toString(bb);
    if (!StringUtils.isEmpty(p)) {
      return gsonCustom.fromJson(p, ServiceResourceProfile.class);
    } else {
      return null;
    }
  }

  public static Constraint getConstraint(ByteBuffer bb) {
    Constraint.Type type = Constraint.Type.values()[bb.getInt()];
    String p = toString(bb);
    switch (type) {
      case NULL:
        return null;

      case LIKE:

        if (!StringUtils.isEmpty(p)) {
          return gson.fromJson(p, LikeConstraint.class);
        }
    }
    return null;
  }

  public static Protos.SlaveID toSlaveId(ByteBuffer bb) {
    int size = bb.getInt();
    if (size > 0) {
      try {
        return Protos.SlaveID.parseFrom(getBytes(bb, size));
      } catch (Exception e) {
        throw new RuntimeException("ByteBuffer not in expected format," + " failed to parse SlaveId bytes", e);
      }
    } else {
      return null;
    }
  }

  public static Protos.TaskStatus toTaskStatus(ByteBuffer bb) {
    int size = bb.getInt();
    if (size > 0) {
      try {
        return Protos.TaskStatus.parseFrom(getBytes(bb, size));
      } catch (Exception e) {
        throw new RuntimeException("ByteBuffer not in expected format," + " failed to parse TaskStatus bytes", e);
      }
    } else {
      return null;
    }
  }

  public static Protos.ExecutorInfo toExecutorInfo(ByteBuffer bb) {
    int size = bb.getInt();
    if (size > 0) {
      try {
        return Protos.ExecutorInfo.parseFrom(getBytes(bb, size));
      } catch (Exception e) {
        throw new RuntimeException("ByteBuffer not in expected format," + " failed to parse ExecutorInfo bytes", e);
      }
    } else {
      return null;
    }
  }

  public static ByteBuffer fillBuffer(byte src[]) {
    ByteBuffer bb = createBuffer(src.length);
    bb.put(src);
    bb.rewind();
    return bb;
  }

  public static List<ByteBuffer> createBufferList(ByteBuffer bb, int size) {
    List<ByteBuffer> list = new ArrayList<ByteBuffer>(size);
    for (int i = 0; i < size; i++) {
      list.add(fillBuffer(getBytes(bb, bb.getInt())));
    }
    return list;
  }

  private static ByteBuffer createBuffer(int size) {
    return ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
  }

  public static ByteBuffer createBuffer(ByteBuffer bb) {
    return fillBuffer(getBytes(bb, bb.getInt()));
  }

}
