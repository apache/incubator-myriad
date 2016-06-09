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
package org.apache.myriad.scheduler.resource;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import org.apache.mesos.Protos;

import java.util.*;

/**
 * Mutable POJO for handling RangeResources, specifically ports.
 */

public class RangeResource {
  private String name;
  private List<Range> ranges = new ArrayList<>();
  private Long numValues = 0L;
  private Long numDefaultValues = 0L;

  private String role;
  @VisibleForTesting //This way we can set a seed to get deterministic values
  private Random random = new Random(102);

  public RangeResource(String name, String role) {
    this.name = name;
    this.role = role;
  }

  public boolean satisfies(Collection<Long> requestedValues) {
    if (requestedValues.size() > numValues) {
      return false;
    }
    List<Long> tmp = new ArrayList<>();
    tmp.addAll(requestedValues);
    tmp.removeAll(Collections.singleton(0L));
    for (Long val : tmp) {
      if (!contains(val)) {
        return false;
      }
    }
    return true;
  }

  public boolean contains(Long value) {
    for (Range range: ranges) {
      if (range.contains(value)) {
        return true;
      }
    }
    return false;
  }

  public List<Long> getValues() {
    List<Long> ret = new ArrayList<>();
    for (Range range: ranges) {
      ret.addAll(range.allValues());
    }
    return ret;
  }

  public void addRanges(List <Protos.Value.Range> ranges, Boolean withRole) {
    for (Protos.Value.Range range : ranges) {
      long tb = range.getBegin();
      long te = range.getEnd();
      this.ranges.add(new Range(tb, te, withRole));
      numValues += (te - tb + 1);
      if (!withRole) {
        numDefaultValues += (te - tb + 1);
      }
    }
  }

  public List<Protos.Resource> consumeResource(Collection<Long> requestedValues) {
    Preconditions.checkState(satisfies(requestedValues));
    List<Protos.Resource> resources = new ArrayList<>();
    List<Long> nonZeros = new ArrayList<>();
    nonZeros.addAll(requestedValues);
    nonZeros.removeAll(Collections.singleton(0L));
    for (Long value : nonZeros) {
      resources.add(createResource(value, hasRole(value)));
    }
    List<Long> randomValues = getRandomValues(requestedValues.size() - nonZeros.size());
    for (Long value: randomValues) {
      resources.add(createResource(value, false));
    }
    return resources;
  }

  private Protos.Resource createResource(Long value, Boolean withRole) {
    Preconditions.checkState(removeValue(value), "Value " + value + " doesn't exist");
    Protos.Resource.Builder builder = Protos.Resource.newBuilder()
        .setName(name)
        .setType(Protos.Value.Type.RANGES)
        .setRanges(Protos.Value.Ranges.newBuilder()
            .addRange(Protos.Value.Range.newBuilder()
                .setBegin(value)
                .setEnd(value)
                .build()
            )
        );
    if (withRole) {
      builder.setRole(role);
    }
    return builder.build();
  }

  private List<Long> getRandomValues(int size) {
    //can improve this
    List<Integer> sample = new ArrayList<>(size);
    while (sample.size() < size) {
      int rand = random.nextInt(numDefaultValues.intValue());
      if (!sample.contains(rand)) {
        sample.add(rand);
      }
    }
    Collections.sort(sample);

    long location = 0;
    long lastLocation = 0;
    int j = 0;
    List<Long> elems = new ArrayList<>();
    for (Range range : ranges) {
      if (!range.role) {
        long tb = range.begin;
        long te = range.end;
        location += te - tb + 1;
        for (int i = j; i < sample.size(); i++) {
          long val = sample.get(i);
          if (val < location) {
            elems.add(tb + val - lastLocation);
            j++;
          } else {
            lastLocation = location;
            break;
          }
        }
      }
    }
    return elems;
  }

  private boolean removeValue(Long value) {
    for (Range range : ranges) {
      if (range.contains(value)) {
        ranges.remove(range);
        long begin = range.begin;
        long end = range.end;
        if (value != begin && value != end) {
          ranges.add(new Range(begin, value - 1, range.role));
          ranges.add(new Range(value + 1, end, range.role));
          return true;
        } else if (value == begin && value != end) {
          ranges.add(new Range(value + 1, end, range.role));
          return true;
        } else if (value == end && value != begin) {
          ranges.add(new Range(begin, value - 1, range.role));
          return true;
        } else {
          return true;
        }
      }
    }
    return false;
  }

  private boolean hasRole(Long value) {
    for (Range range : ranges) {
      if (range.contains(value)) {
        return range.role;
      }
    }
    return false;
  }

  private static class Range {
    Long begin;
    Long end;
    Boolean role;

    public Range(Long begin, Long end, Boolean role){
      this.begin = begin;
      this.end = end;
      this.role = role;
    }
    public Collection<Long> allValues() {
      List<Long> ret = new ArrayList<>();
      for (long i = begin; i <= end; i++) {
        ret.add(i);
      }
      return ret;
    }
    public Boolean contains(Long value) {
      return (value >= begin && value <= end);
    }

    public String toString() {
      return "(" + begin + "," + end + ")";
    }
  }

}
