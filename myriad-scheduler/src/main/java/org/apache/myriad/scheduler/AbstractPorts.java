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

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.mesos.Protos;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Mutable Collection object for storing Port Resources
 * Not Thread Safe (backed by ArrayList)
 */
public class AbstractPorts {
  /**
   * Immutable POJO for keeping track of port resources
   */
  public static class PortResource {
    private final long port;
    private final String role;

    public PortResource(long port) {
      this.port = port;
      this.role = null;
    }

    public PortResource(long port, String role) {
      this.port = port;
      this.role = role;
    }

    public long getPort() {
      return port;
    }

    public Optional<String> getRole() {
      return Optional.fromNullable(role);
    }

  }

  List<PortResource> portList = Lists.newArrayList();

  public AbstractPorts() {
    portList = Lists.newArrayList();
  }

  public AbstractPorts(int len) {
    portList = new ArrayList<PortResource>(len);
  }

  public int size() {
    return portList.size();
  }

  public boolean isEmpty() {
    return portList.isEmpty();
  }

  public void add(int index, long port, String role) {
    portList.add(index, new PortResource(port, role));
  }

  public void add(int index, long port) {
    portList.add(index, new PortResource(port));
  }

  public boolean add(long port) {
    return portList.add(new PortResource(port));
  }

  public boolean add(long port, String role) {
    return portList.add(new PortResource(port, role));
  }

  public boolean add(PortResource portResource) {
    return portList.add(portResource);
  }

  public PortResource get(int index) {
    return portList.get(index);
  }

  public int indexOf(Object o) {
    return portList.indexOf(o);
  }

  public int lastIndexOf(Object o) {
    return portList.lastIndexOf(o);
  }

  public ListIterator<PortResource> listIterator() {
    return portList.listIterator();
  }

  public ListIterator<PortResource> listIterator(int index) {
    return portList.listIterator(index);
  }

  public Iterable<Protos.Resource> createResourceList() {
    return Iterables.transform(portList, new Function<PortResource, Protos.Resource>() {
      @Nullable
      @Override
      public Protos.Resource apply(PortResource portResource) {
        Protos.Resource.Builder resourceBuilder = Protos.Resource.newBuilder()
            .setName("ports")
            .setType(Protos.Value.Type.RANGES)
            .setRanges(
                Protos.Value.Ranges.newBuilder().addRange(
                    Protos.Value.Range.newBuilder()
                        .setBegin(portResource.getPort())
                        .setEnd(portResource.getPort())
                        .build()
                ));
        if (portResource.getRole().isPresent()) {
          resourceBuilder.setRole(portResource.getRole().get());
        }
        return resourceBuilder.build();
      }
    });
  }
}
