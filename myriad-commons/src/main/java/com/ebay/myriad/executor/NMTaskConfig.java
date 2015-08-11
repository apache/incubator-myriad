package com.ebay.myriad.executor;

import java.util.Map;

/**
 * Node Manger Task Configuraiton
 */
public class NMTaskConfig {
    private String yarnHome;
    private Long advertisableCpus;
    private Long advertisableMem;
    private String jvmOpts;
    private Boolean cgroups;
    private Long rpcPort;
    private Long localizerPort;
    private Long webAppHttpPort;
    private Long shufflePort;

    private Map<String, String> yarnEnvironment;

    public String getYarnHome() {
        return yarnHome;
    }

    public void setYarnHome(String yarnHome) {
        this.yarnHome = yarnHome;
    }

    public Long getAdvertisableCpus() {
        return advertisableCpus;
    }

    public void setAdvertisableCpus(Long advertisableCpus) {
        this.advertisableCpus = advertisableCpus;
    }

    public Long getAdvertisableMem() {
        return advertisableMem;
    }

    public void setAdvertisableMem(Long advertisableMem) {
        this.advertisableMem = advertisableMem;
    }

    public String getJvmOpts() {
        return jvmOpts;
    }

    public void setJvmOpts(String jvmOpts) {
        this.jvmOpts = jvmOpts;
    }

    public Boolean getCgroups() {
        return cgroups;
    }

    public void setCgroups(Boolean cgroups) {
        this.cgroups = cgroups;
    }

    public Map<String, String> getYarnEnvironment() {
        return yarnEnvironment;
    }

    public void setYarnEnvironment(Map<String, String> yarnEnvironment) {
        this.yarnEnvironment = yarnEnvironment;
    }

    public Long getRpcPort() {
        return rpcPort;
    }

    public void setRpcPort(long port) {
        rpcPort = port;
    }

    public Long gettWebAppHttpPort() {
        return webAppHttpPort;
    }

    public void setWebAppHttpPort(Long port) {
        webAppHttpPort = port;
    }

    public Long getLocalizerPort() {
        return localizerPort;
    }

    public void setLocalizerPort(Long localizerPort) {
        this.localizerPort = localizerPort;
    }

    public Long getShufflePort() {
        return shufflePort;
    }

    public void setShufflePort(Long shufflePort) {
        this.shufflePort = shufflePort;
    }

}
