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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.mesos.Protos;
import org.apache.myriad.configuration.MyriadConfiguration;
import org.apache.myriad.configuration.MyriadExecutorConfiguration;
import org.apache.myriad.configuration.ServiceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Interface to plugin multiple implementations for executor command generation
 */
public abstract class ExecutorCommandLineGenerator {

  protected static final Logger LOGGER = LoggerFactory.getLogger(NMExecutorCommandLineGenerator.class);

  public static final String KEY_YARN_RM_HOSTNAME = "yarn.resourcemanager.hostname";
  public static final String KEY_YARN_HOME = "yarn.home";

  protected static final String ALL_LOCAL_IPV4ADDR = "0.0.0.0:";

  protected static final String PROPERTY_FORMAT = " -D%s=%s ";
  protected static final String CMD_FORMAT = "echo \"%1$s\" && %1$s";

  protected Protos.CommandInfo staticCommandInfo;

  protected MyriadConfiguration myriadConfiguration;
  protected MyriadExecutorConfiguration myriadExecutorConfiguration;
  protected YarnConfiguration yarnConfiguration = new YarnConfiguration();

  abstract Protos.CommandInfo generateCommandLine(ServiceResourceProfile profile, ServiceConfiguration serviceConfiguration, Collection<Long> ports);

  protected void appendDistroExtractionCommands(StringBuilder cmdLine) {
    /*
    TODO(darinj): Overall this is messier than I'd like. We can't let mesos untar the distribution, since
    it will change the permissions.  Instead we simply download the tarball and execute tar -xvpf. We also
    pull the config from the resource manager and put them in the yarnConfiguration dir.  This is also why we need
    frameworkSuperUser. This will be refactored after Mesos-1790 is resolved.
   */

    //TODO(DarinJ) support other compression, as this is a temp fix for Mesos 1760 may not get to it.
    if (myriadExecutorConfiguration.getNodeManagerUri().isPresent()) {
      //Extract tarball keeping permissions, necessary to keep HADOOP_HOME/bin/container-executor suidbit set.
      //If SudoUser not enable LinuxExecutor will not work
      appendSudo(cmdLine);
      cmdLine.append("tar -zxpf ").append(getFileName(myriadExecutorConfiguration.getNodeManagerUri().get()));
      cmdLine.append(" && ");
      //Place the hadoop config where in the HADOOP_CONF_DIR where it will be read by the NodeManager
      //The url for the resource manager config is: http(s)://hostname:port/yarnConfiguration so fetcher.cpp downloads the
      //config file to yarnConfiguration, It's an xml file with the parameters of yarn-site.xml, core-site.xml and hdfs.xml.
      if (!myriadExecutorConfiguration.getConfigUri().isPresent()) {
        appendSudo(cmdLine);
        cmdLine.append(" cp yarnConfiguration ");
        cmdLine.append(myriadConfiguration.getYarnEnvironment().get("YARN_HOME"));
        cmdLine.append("/etc/hadoop/yarn-site.xml && ");
      }
    }
  }

  protected void addJavaOpt(StringBuilder opts, String propertyName, String propertyValue) {
    String envOpt = String.format(PROPERTY_FORMAT, propertyName, propertyValue);
    opts.append(envOpt);
  }

  protected void appendSudo(StringBuilder cmdLine) {
    if (myriadConfiguration.getFrameworkSuperUser().isPresent()) {
      cmdLine.append(" sudo ");
    }
  }

  protected void appendUserSudo(StringBuilder cmdLine) {
    if (myriadConfiguration.getFrameworkSuperUser().isPresent()) {
      cmdLine.append(" sudo -E -u ");
      cmdLine.append(myriadConfiguration.getFrameworkUser().get());
      cmdLine.append(" -H ");
    }
  }

  public String getConfigurationUrl() {
    String httpPolicy = yarnConfiguration.get(TaskFactory.YARN_HTTP_POLICY);
    String address = StringUtils.EMPTY;
    if (httpPolicy != null && httpPolicy.equals(TaskFactory.YARN_HTTP_POLICY_HTTPS_ONLY)) {
      address = yarnConfiguration.get(TaskFactory.YARN_RESOURCEMANAGER_WEBAPP_HTTPS_ADDRESS);
      if (StringUtils.isEmpty(address)) {
        address = yarnConfiguration.get(TaskFactory.YARN_RESOURCEMANAGER_HOSTNAME) + ":8090";
      }
      return "https://" + address + "/yarnConfiguration";
    } else {
      address = yarnConfiguration.get(TaskFactory.YARN_RESOURCEMANAGER_WEBAPP_ADDRESS);
      if (StringUtils.isEmpty(address)) {
        address = yarnConfiguration.get(TaskFactory.YARN_RESOURCEMANAGER_HOSTNAME) + ":8088";
      }
      return "http://" + address + "/yarnConfiguration";
    }
  }

  private static String getFileName(String uri) {
    int lastSlash = uri.lastIndexOf('/');
    if (lastSlash == -1) {
      return uri;
    } else {
      String fileName = uri.substring(lastSlash + 1);
      Preconditions.checkArgument(!Strings.isNullOrEmpty(fileName), "URI should not have a slash at the end");
      return fileName;
    }
  }

  protected String getUser() {
    if (myriadConfiguration.getFrameworkSuperUser().isPresent()) {
      return myriadConfiguration.getFrameworkSuperUser().get();
    } else {
      return myriadConfiguration.getFrameworkUser().get();
    }
  }

  protected List<Protos.CommandInfo.URI> getUris() {
    List<Protos.CommandInfo.URI> uris = new ArrayList<>();
    if (myriadExecutorConfiguration.getJvmUri().isPresent()) {
      final String jvmRemoteUri = myriadExecutorConfiguration.getJvmUri().get();
      LOGGER.info("Getting JRE distribution from:" + jvmRemoteUri);
      uris.add(Protos.CommandInfo.URI.newBuilder().setValue(jvmRemoteUri).build());
    }
    if (myriadExecutorConfiguration.getConfigUri().isPresent()) {
      String configURI = myriadExecutorConfiguration.getConfigUri().get();
      LOGGER.info("Getting Hadoop configuration from: {}", configURI);
      uris.add(Protos.CommandInfo.URI.newBuilder().setValue(configURI).build());
    } else if (myriadExecutorConfiguration.getNodeManagerUri().isPresent()) {
      String configURI = getConfigurationUrl();
      LOGGER.info("Getting Hadoop configuration from: {}", configURI);
      uris.add(Protos.CommandInfo.URI.newBuilder().setValue(configURI).build());
    }
    if (myriadExecutorConfiguration.getNodeManagerUri().isPresent()) {
      //Both FrameworkUser and FrameworkSuperuser to get all of the directory permissions correct.
      if (!(myriadConfiguration.getFrameworkUser().isPresent() && myriadConfiguration.getFrameworkSuperUser().isPresent())) {
        LOGGER.warn("Trying to use remote distribution, but frameworkUser and/or frameworkSuperUser not set!" +
            "Some features may not work");
      }
      String nodeManagerUri = myriadExecutorConfiguration.getNodeManagerUri().get();
      LOGGER.info("Getting Hadoop distribution from: {}", nodeManagerUri);
      uris.add(Protos.CommandInfo.URI.newBuilder().setValue(nodeManagerUri).setExtract(false).build());
    }
    return uris;
  }
}
