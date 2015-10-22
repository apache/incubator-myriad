package org.apache.myriad.scheduler.constraints;

/**
 * Interface for Constraint.
 */
public interface Constraint {
  /**
   * Type of Constraint
   */
  enum Type {
    NULL, // to help with serialization
    LIKE
  }

  public Type getType();

}
