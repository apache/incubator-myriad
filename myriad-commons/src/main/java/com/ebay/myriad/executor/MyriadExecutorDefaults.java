package com.ebay.myriad.executor;

/**
 * Myriad's Executor Defaults
 */
public class MyriadExecutorDefaults {
    public static final String ENV_YARN_NODEMANAGER_OPTS = "YARN_NODEMANAGER_OPTS";

    /**
     * YARN container executor class.
     */
    public static final String KEY_YARN_NM_CONTAINER_EXECUTOR_CLASS = "yarn.nodemanager.container-executor.class";

    public static final String VAL_YARN_NM_CONTAINER_EXECUTOR_CLASS = "org.apache.hadoop.yarn.server.nodemanager.LinuxContainerExecutor";

    public static final String DEFAULT_YARN_NM_CONTAINER_EXECUTOR_CLASS = "org.apache.hadoop.yarn.server.nodemanager.DefaultContainerExecutor";

    /**
     * YARN class to help handle LCE resources
     */
    public static final String KEY_YARN_NM_LCE_RH_CLASS = "yarn.nodemanager.linux-container-executor.resources-handler.class";

    public static final String VAL_YARN_NM_LCE_RH_CLASS = "org.apache.hadoop.yarn.server.nodemanager.util.CgroupsLCEResourcesHandler";

    public static final String KEY_YARN_NM_LCE_CGROUPS_HIERARCHY = "yarn.nodemanager.linux-container-executor.cgroups.hierarchy";

    public static final String KEY_YARN_NM_LCE_CGROUPS_MOUNT = "yarn.nodemanager.linux-container-executor.cgroups.mount";

    public static final String KEY_YARN_NM_LCE_CGROUPS_MOUNT_PATH = "yarn.nodemanager.linux-container-executor.cgroups.mount-path";

    public static final String KEY_YARN_NM_LCE_GROUP = "yarn.nodemanager.linux-container-executor.group";

    public static final String KEY_YARN_NM_LCE_PATH = "yarn.nodemanager.linux-container-executor.path";

    public static final String KEY_YARN_HOME = "yarn.home";

    public static final String KEY_NM_RESOURCE_CPU_VCORES = "nodemanager.resource.cpu-vcores";

    public static final String KEY_NM_RESOURCE_MEM_MB = "nodemanager.resource.memory-mb";

    /**
     * Allot 10% more memory to account for JVM overhead.
     */
    public static final double JVM_OVERHEAD = 0.1;

    /**
     * Default -Xmx for executor JVM.
     */

    public static final double DEFAULT_JVM_MAX_MEMORY_MB = 256;
    /**
     * Default cpus for executor JVM.
     */
    public static final double DEFAULT_CPUS = 0.2;


}
