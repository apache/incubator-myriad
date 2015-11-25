package org.apache.myriad.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;


/**
 * Created by darinj on 11/4/15.
 */
public class MyriadMesosContainerConfiguration {
  @JsonProperty
  @NotEmpty
  String image;

  public String getImage() {
    return image;
  }

}
