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
package org.apache.myriad.scheduler.constraints;

import com.google.gson.Gson;
import java.util.Collection;
import java.util.regex.Pattern;
import org.apache.mesos.Protos.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Constraint for LIKE operator.
 * Format: <mesos_slave_attribute|hostname> LIKE <regex_value>
 */
public class LikeConstraint implements Constraint {
  private static final Logger LOGGER = LoggerFactory.getLogger(LikeConstraint.class);

  private static final String HOSTNAME = "hostname";

  private final String lhs;
  private final Pattern pattern;

  public LikeConstraint(String lhs, String rhsRegex) {
    this.lhs = lhs;
    this.pattern = Pattern.compile(rhsRegex);
  }

  public boolean isConstraintOnHostName() {
    return lhs.equalsIgnoreCase(HOSTNAME);
  }

  public boolean matchesHostName(String hostname) {
    return lhs.equalsIgnoreCase(HOSTNAME) && hostname != null && pattern.matcher(hostname).matches();
  }

  public boolean matchesSlaveAttributes(Collection<Attribute> attributes) {
    if (!lhs.equalsIgnoreCase(HOSTNAME) && attributes != null) {
      for (Attribute attr : attributes) {
        if (attr.getName().equalsIgnoreCase(lhs)) {
          switch (attr.getType()) {
            case TEXT:
              return this.pattern.matcher(attr.getText().getValue()).matches();

            case SCALAR:
              return this.pattern.matcher(String.valueOf(attr.getScalar().getValue())).matches();

            default:
              LOGGER.warn("LIKE constraint currently doesn't support Mesos slave attributes " + "of type {}. Attribute Name: {}",
                  attr.getType(), attr.getName());
              return false;

          }
        }
      }
    }
    return false;
  }

  @Override
  public Type getType() {
    return Type.LIKE;
  }

  @Override
  public String toString() {
    Gson gson = new Gson();
    return gson.toJson(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LikeConstraint)) {
      return false;
    }

    LikeConstraint that = (LikeConstraint) o;

    if (lhs != null ? !lhs.equals(that.lhs) : that.lhs != null) {
      return false;
    }
    if (pattern != null ? !pattern.pattern().equals(that.pattern.pattern()) : that.pattern != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = lhs != null ? lhs.hashCode() : 0;
    result = 31 * result + (pattern != null ? pattern.pattern().hashCode() : 0);
    return result;
  }
}
