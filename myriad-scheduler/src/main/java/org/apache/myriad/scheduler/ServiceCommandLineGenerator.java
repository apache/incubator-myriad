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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.myriad.scheduler;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;
import org.apache.mesos.Protos;
import org.apache.myriad.configuration.MyriadConfiguration;
import org.apache.myriad.configuration.ServiceConfiguration;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/**
 * CommandLineGenerator for any aux service launched by Myriad as binary distro
 */
public class ServiceCommandLineGenerator extends ExecutorCommandLineGenerator {

  public static final String ENV_HADOOP_OPTS = "HADOOP_OPTS";

  private String baseCmd;

  public ServiceCommandLineGenerator(MyriadConfiguration cfg) {
    this.myriadConfiguration = cfg;
    myriadExecutorConfiguration = cfg.getMyriadExecutorConfiguration();
    generateStaticCommandLine();
  }

  protected void generateStaticCommandLine() {
    Protos.CommandInfo.Builder builder = Protos.CommandInfo.newBuilder();
    builder.addAllUris(getUris());
    builder.setUser(getUser());
    staticCommandInfo = builder.build();

    StringBuilder cmdLine = new StringBuilder();
    appendDistroExtractionCommands(cmdLine);
    appendUserSudo(cmdLine);
    baseCmd = cmdLine.toString();
  }

  @Override
  public Protos.CommandInfo generateCommandLine(ServiceResourceProfile profile,
                                                ServiceConfiguration serviceConfiguration,
                                                Collection<Long> ports) {
    Protos.CommandInfo.Builder builder = Protos.CommandInfo.newBuilder();
    builder.mergeFrom(staticCommandInfo);
    builder.setValue(String.format(CMD_FORMAT, baseCmd + " " + serviceConfiguration.getCommand().get()));
    builder.setEnvironment(generateEnvironment(profile, ports));
    return builder.build();
  }

  protected Protos.Environment generateEnvironment(ServiceResourceProfile serviceResourceProfile, Collection<Long> ports) {
    Map<String, String> yarnEnv = myriadConfiguration.getYarnEnvironment();
    Protos.Environment.Builder builder = Protos.Environment.newBuilder();

    builder.addAllVariables(Iterables.transform(yarnEnv.entrySet(), new Function<Map.Entry<String, String>, Protos.Environment.Variable>() {
      public Protos.Environment.Variable apply(Map.Entry<String, String> x) {
        return Protos.Environment.Variable.newBuilder().setName(x.getKey()).setValue(x.getValue()).build();
      }
    }));

    StringBuilder hadoopOpts = new StringBuilder();
    String rmHostName = System.getProperty(KEY_YARN_RM_HOSTNAME);

    if (StringUtils.isNotEmpty(rmHostName)) {
      addJavaOpt(hadoopOpts, KEY_YARN_RM_HOSTNAME, rmHostName);
    }

    if (yarnEnv.containsKey(KEY_YARN_HOME)) {
      addJavaOpt(hadoopOpts, KEY_YARN_HOME, yarnEnv.get("YARN_HOME"));
    }

    Map<String, Long> portsMap = serviceResourceProfile.getPorts();
    Preconditions.checkState(portsMap.size() == ports.size());
    Iterator itr = ports.iterator();
    for (String portProperty : portsMap.keySet()) {
      addJavaOpt(hadoopOpts, portProperty, ALL_LOCAL_IPV4ADDR + itr.next());
    }

    if (myriadConfiguration.getYarnEnvironment().containsKey(ENV_HADOOP_OPTS)) {
      hadoopOpts.append(" ").append(yarnEnv.get(ENV_HADOOP_OPTS));
    }
    builder.addAllVariables(Collections.singleton(
            Protos.Environment.Variable.newBuilder()
                .setName(ENV_HADOOP_OPTS)
                .setValue(hadoopOpts.toString()).build())
    );
    return builder.build();
  }

}
