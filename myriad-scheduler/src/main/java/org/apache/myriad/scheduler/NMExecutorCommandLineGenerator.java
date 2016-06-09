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

package org.apache.myriad.scheduler;


import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;



import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;
import org.apache.mesos.Protos;
import org.apache.myriad.configuration.MyriadConfiguration;
import org.apache.myriad.configuration.ServiceConfiguration;

import org.apache.mesos.Protos.CommandInfo;

/**
 * Implementation assumes NM binaries already deployed
 */
public class NMExecutorCommandLineGenerator extends ExecutorCommandLineGenerator {

  /**
   * YARN class to help handle LCE resources
   */
  public static final String ENV_YARN_NODEMANAGER_OPTS = "YARN_NODEMANAGER_OPTS";
  public static final String KEY_YARN_NM_LCE_CGROUPS_HIERARCHY = "yarn.nodemanager.linux-container-executor.cgroups.hierarchy";
  public static final String KEY_NM_RESOURCE_CPU_VCORES = "nodemanager.resource.cpu-vcores";
  public static final String KEY_NM_RESOURCE_MEM_MB = "nodemanager.resource.memory-mb";

  public static final String YARN_NM_CMD = " $YARN_HOME/bin/yarn nodemanager";

  public NMExecutorCommandLineGenerator(MyriadConfiguration cfg) {
    this.myriadConfiguration = cfg;
    this.myriadExecutorConfiguration = cfg.getMyriadExecutorConfiguration();
    generateStaticCommandLine();
  }

  @Override
  CommandInfo generateCommandLine(ServiceResourceProfile profile,
                                  ServiceConfiguration serviceConfiguration, Collection<Long> ports) {
    CommandInfo.Builder builder = CommandInfo.newBuilder();
    builder.mergeFrom(staticCommandInfo);
    builder.setEnvironment(generateEnvironment(profile, ports));
    builder.setUser(getUser());
    return builder.build();
  }

  protected void generateStaticCommandLine() {
    CommandInfo.Builder builder = CommandInfo.newBuilder();
    StringBuilder cmdLine = new StringBuilder();
    appendCgroupsCmds(cmdLine);
    appendDistroExtractionCommands(cmdLine);
    appendUserSudo(cmdLine);
    cmdLine.append(YARN_NM_CMD);
    builder.setValue(String.format(CMD_FORMAT, cmdLine.toString()));
    builder.addAllUris(getUris());
    staticCommandInfo = builder.build();
  }

  protected Protos.Environment generateEnvironment(ServiceResourceProfile profile, Collection<Long> ports) {
    Map<String, String> yarnEnv = myriadConfiguration.getYarnEnvironment();
    Protos.Environment.Builder builder = Protos.Environment.newBuilder();
    builder.addAllVariables(Iterables.transform(yarnEnv.entrySet(), new Function<Map.Entry<String, String>, Protos.Environment.Variable>() {
      public Protos.Environment.Variable apply(Map.Entry<String, String> x) {
        return Protos.Environment.Variable.newBuilder().setName(x.getKey()).setValue(x.getValue()).build();
      }
    }));

    StringBuilder yarnOpts = new StringBuilder();
    String rmHostName = System.getProperty(KEY_YARN_RM_HOSTNAME);


    if (StringUtils.isNotEmpty(rmHostName)) {
      addJavaOpt(yarnOpts, KEY_YARN_RM_HOSTNAME, rmHostName);
    }

    if (yarnEnv.containsKey(KEY_YARN_HOME)) {
      addJavaOpt(yarnOpts, KEY_YARN_HOME, yarnEnv.get("YARN_HOME"));
    }

    addJavaOpt(yarnOpts, KEY_NM_RESOURCE_CPU_VCORES, Integer.toString(profile.getCpus().intValue()));
    addJavaOpt(yarnOpts, KEY_NM_RESOURCE_MEM_MB, Integer.toString(profile.getMemory().intValue()));
    Map<String, Long> portsMap = profile.getPorts();
    Preconditions.checkState(portsMap.size() == ports.size());

    Iterator itr = ports.iterator();
    for (String portProperty : portsMap.keySet()) {
      if (portProperty.endsWith("address")) {
        addJavaOpt(yarnOpts, portProperty, ALL_LOCAL_IPV4ADDR + itr.next());
      } else if (portProperty.endsWith("port")) {
        addJavaOpt(yarnOpts, portProperty, itr.next().toString());
      } else {
        LOGGER.warn("{} propery isn't an address or port!", portProperty);
      }
    }


    if (myriadConfiguration.getYarnEnvironment().containsKey(ENV_YARN_NODEMANAGER_OPTS)) {
      yarnOpts.append(" ").append(yarnEnv.get(ENV_YARN_NODEMANAGER_OPTS));
    }
    builder.addAllVariables(Collections.singleton(
            Protos.Environment.Variable.newBuilder()
                .setName(ENV_YARN_NODEMANAGER_OPTS)
                .setValue(yarnOpts.toString()).build())
    );
    return builder.build();
  }

  protected void appendCgroupsCmds(StringBuilder cmdLine) {
    //These can't be set in the environment as they require commands to be run on the host
    if (myriadConfiguration.getFrameworkSuperUser().isPresent() && myriadConfiguration.isCgroupEnabled()) {
      cmdLine.append(" export TASK_DIR=`cat /proc/self/cgroup | grep :cpu: | cut -d: -f3` &&");
      //The container executor script expects mount-path to exist and owned by the yarn user
      //See: https://hadoop.apache.org/docs/stable/hadoop-yarn/hadoop-yarn-site/NodeManagerCgroups.html
      //If YARN ever moves to cgroup/mem it will be necessary to add a mem version.
      appendSudo(cmdLine);
      cmdLine.append("chown " + myriadConfiguration.getFrameworkUser().get() + " ");
      cmdLine.append(myriadConfiguration.getCGroupPath());
      cmdLine.append("/cpu$TASK_DIR &&");
      cmdLine.append(String.format("export %s=\"$%s -D%s=%s\" && ", ENV_YARN_NODEMANAGER_OPTS, ENV_YARN_NODEMANAGER_OPTS,
          KEY_YARN_NM_LCE_CGROUPS_HIERARCHY, "$TASK_DIR"));
    } else if (myriadConfiguration.isCgroupEnabled()) {
      LOGGER.info("frameworkSuperUser not set ignoring cgroup configuration, this will likely can the nodemanager to crash");
    }
  }

}
