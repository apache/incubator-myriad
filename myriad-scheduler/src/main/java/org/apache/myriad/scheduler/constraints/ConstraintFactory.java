package org.apache.myriad.scheduler.constraints;

/**
 * Factory to create constraints.
 */
public class ConstraintFactory {

  public static Constraint createConstraint(String constraintStr) {
    if (constraintStr != null) {
      String[] splits = constraintStr.split(" LIKE ");
      if (splits.length == 2) {
        return new LikeConstraint(splits[0], splits[1]);
      }
    }
    return null;
  }

}
