package com.ebay.myriad.scheduler;

import com.google.gson.Gson;

/**
 * Extended ServiceResourceProfile for services that need to pass set of resources downstream
 * currently the only such service is NodeManager
 *
 */
public class ExtendedResourceProfile extends ServiceResourceProfile {

  private NMProfile childProfile;

  /**
   * 
   * @param childProfile - should be null
   * @param cpu
   * @param mem
   * will throw NullPoiterException if childProfile is null
   */
  public ExtendedResourceProfile(NMProfile childProfile, Double cpu, Double mem) {
    super(childProfile.getName(), cpu, mem);
    this.childProfile = childProfile;
    this.className = ExtendedResourceProfile.class.getName();
  }

  public NMProfile getChildProfile() {
    return childProfile;
  }

  public void setChildProfile(NMProfile nmProfile) {
    this.childProfile = nmProfile;
  }
  
  @Override
  public String getName() {
    return childProfile.getName();
  }

  @Override
  public Double getCpus() {
    return childProfile.getCpus().doubleValue();
  }

  @Override
  public Double getMemory() {
    return childProfile.getMemory().doubleValue();
  }

  @Override
  public Double getAggregateMemory() {
    return memory + childProfile.getMemory();
  }
  
  @Override
  public Double getAggregateCpu() {
    return cpus + childProfile.getCpus();
  }

  @Override
  public String toString() {
      Gson gson = new Gson();
      return gson.toJson(this);
  }
}
