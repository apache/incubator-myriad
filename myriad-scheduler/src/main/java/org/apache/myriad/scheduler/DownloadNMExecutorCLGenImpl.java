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
import org.apache.myriad.configuration.MyriadConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation assumes NM binaries will be downloaded
 */
public class DownloadNMExecutorCLGenImpl extends NMExecutorCLGenImpl {

  private static final Logger LOGGER = LoggerFactory.
      getLogger(DownloadNMExecutorCLGenImpl.class);

  private final String nodeManagerUri;

  public DownloadNMExecutorCLGenImpl(MyriadConfiguration cfg, String nodeManagerUri) {
    super(cfg);
    this.nodeManagerUri = nodeManagerUri;
  }

  @Override
  public String generateCommandLine(ServiceResourceProfile profile, Ports ports) {
    StringBuilder cmdLine = new StringBuilder();
    LOGGER.info("Using remote distribution");
    generateEnvironment(profile, (NMPorts) ports);
    appendDistroExtractionCommands(cmdLine);
    appendCgroupsCmds(cmdLine);
    appendYarnHomeExport(cmdLine);
    appendUserSudo(cmdLine);
    appendEnvForNM(cmdLine);
    cmdLine.append(YARN_NM_CMD);
    return cmdLine.toString();
  }

  protected void appendDistroExtractionCommands(StringBuilder cmdLine) {
    /*
    TODO(darinj): Overall this is messier than I'd like. We can't let mesos untar the distribution, since
    it will change the permissions.  Instead we simply download the tarball and execute tar -xvpf. We also
    pull the config from the resource manager and put them in the conf dir.  This is also why we need
    frameworkSuperUser. This will be refactored after Mesos-1790 is resolved.
   */

    //TODO(DarinJ) support other compression, as this is a temp fix for Mesos 1760 may not get to it.
    //Extract tarball keeping permissions, necessary to keep HADOOP_HOME/bin/container-executor suidbit set.
    appendSudo(cmdLine);
    cmdLine.append("tar -zxpf ").append(getFileName(nodeManagerUri));
    //Place the hadoop config where in the HADOOP_CONF_DIR where it will be read by the NodeManager
    //The url for the resource manager config is: http(s)://hostname:port/conf so fetcher.cpp downloads the
    //config file to conf, It's an xml file with the parameters of yarn-site.xml, core-site.xml and hdfs.xml.
    cmdLine.append(" && ");
    appendSudo(cmdLine);
    cmdLine.append(" cp conf ");
    cmdLine.append(cfg.getYarnEnvironment().get("YARN_HOME"));
    cmdLine.append("/etc/hadoop/yarn-site.xml;");
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

}
