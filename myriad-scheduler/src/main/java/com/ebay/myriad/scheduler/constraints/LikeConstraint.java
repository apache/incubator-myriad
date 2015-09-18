package com.ebay.myriad.scheduler.constraints;

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

  private String lhs;
  private String rhsRegex;

  public LikeConstraint(String lhs, String rhsRegex) {
    this.lhs = lhs;
    this.rhsRegex = rhsRegex;
  }

  public boolean isConstraintOnHostName() {
    return lhs.equalsIgnoreCase("hostname");
  }

  public boolean matchesHostName(String hostname) {
    return lhs.equalsIgnoreCase("hostname") && hostname != null && Pattern.matches(rhsRegex, hostname);
  }

  public boolean matchesSlaveAttributes(Collection<Attribute> attributes) {
    if (!lhs.equalsIgnoreCase("hostname") && attributes != null) {
      for (Attribute attr : attributes) {
        if (attr.getName().equalsIgnoreCase(lhs)) {
          switch (attr.getType()) {
            case TEXT:
              return Pattern.matches(rhsRegex, attr.getText().getValue());

            case SCALAR:
              return Pattern.matches(rhsRegex, String.valueOf(attr.getScalar().getValue()));

            default:
              LOGGER.warn("LIKE constraint currently doesn't support Mesos slave attributes " +
                  "of type {}. Attribute Name: {}", attr.getType(), attr.getName());
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
    if (rhsRegex != null ? !rhsRegex.equals(that.rhsRegex) : that.rhsRegex != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = lhs != null ? lhs.hashCode() : 0;
    result = 31 * result + (rhsRegex != null ? rhsRegex.hashCode() : 0);
    return result;
  }
}
