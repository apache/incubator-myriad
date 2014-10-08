package com.ebay.myriad.executor;

public class NMTaskConfig {
    private String user;
    private String yarnHome;
    private double advertisableCpus;
    private double advertisableMem;
    private String jvmOpts;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getYarnHome() {
        return yarnHome;
    }

    public void setYarnHome(String yarnHome) {
        this.yarnHome = yarnHome;
    }

    public double getAdvertisableCpus() {
        return advertisableCpus;
    }

    public void setAdvertisableCpus(double advertisableCpus) {
        this.advertisableCpus = advertisableCpus;
    }

    public double getAdvertisableMem() {
        return advertisableMem;
    }

    public void setAdvertisableMem(double advertisableMem) {
        this.advertisableMem = advertisableMem;
    }

    public String getJvmOpts() {
        return jvmOpts;
    }

    public void setJvmOpts(String jvmOpts) {
        this.jvmOpts = jvmOpts;
    }

}
