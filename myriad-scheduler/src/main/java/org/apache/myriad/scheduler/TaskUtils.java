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

import com.google.common.base.Optional;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.mesos.Protos;
import org.apache.myriad.configuration.MyriadBadConfigurationException;
import org.apache.myriad.configuration.MyriadConfiguration;
import org.apache.myriad.configuration.MyriadExecutorConfiguration;
import org.apache.myriad.configuration.NodeManagerConfiguration;
import org.apache.myriad.configuration.ServiceConfiguration;
import org.apache.myriad.executor.MyriadExecutorDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * utility class for working with tasks and node manager profiles
 */
public class TaskUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(TaskUtils.class);

  private static final String YARN_NODEMANAGER_RESOURCE_CPU_VCORES = "yarn.nodemanager.resource.cpu-vcores";
  private static final String YARN_NODEMANAGER_RESOURCE_MEMORY_MB = "yarn.nodemanager.resource.memory-mb";

  private MyriadConfiguration cfg;

  @Inject
  public TaskUtils(MyriadConfiguration cfg) {
    this.cfg = cfg;
  }

  public static String getRevisedConfig(Double cpu, Double memory) {
    String revisedConfig = "";
    try {

      // todo:(kgs) replace with more abstract xml parser
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      DocumentBuilder builder;
      Document doc;

      builder = factory.newDocumentBuilder();
      InputStream resourceAsStream = TaskUtils.class.getClassLoader().getResourceAsStream("yarn-site.xml");

      doc = builder.parse(new InputSource(resourceAsStream));
      resourceAsStream.close();

      XPathFactory xFactory = XPathFactory.newInstance();

      XPath xpath = xFactory.newXPath();
      XPathExpression cpuXpath = xpath.compile("//property/name");
      Object cpuNodeObj = cpuXpath.evaluate(doc, XPathConstants.NODESET);

      NodeList cpuNode = (NodeList) cpuNodeObj;

      for (int i = 0; i < cpuNode.getLength(); i++) {
        Node item = cpuNode.item(i);
        if (YARN_NODEMANAGER_RESOURCE_CPU_VCORES.equals(item.getTextContent())) {
          Node propertyNode = item.getParentNode();
          NodeList childNodes = propertyNode.getChildNodes();
          for (int j = 0; j < childNodes.getLength(); j++) {
            Node item2 = childNodes.item(j);
            if ("value".equals(item2.getNodeName())) {
              item2.setTextContent(cpu.intValue() + "");
            }
          }
        } else if (YARN_NODEMANAGER_RESOURCE_MEMORY_MB.equals(item.getTextContent())) {
          Node propertyNode = item.getParentNode();
          NodeList childNodes = propertyNode.getChildNodes();
          for (int j = 0; j < childNodes.getLength(); j++) {
            Node item2 = childNodes.item(j);
            if ("value".equals(item2.getNodeName())) {
              item2.setTextContent(memory.intValue() + "");
            }
          }
        }
      }

      TransformerFactory tf = TransformerFactory.newInstance();
      Transformer transformer = tf.newTransformer();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      StringWriter writer = new StringWriter();
      transformer.transform(new DOMSource(doc), new StreamResult(writer));

      revisedConfig = writer.getBuffer().toString().replaceAll("\n|\r", "");
    } catch (TransformerConfigurationException e) {
      e.printStackTrace();
    } catch (TransformerException | SAXException | XPathExpressionException | ParserConfigurationException e) {
      LOGGER.error("Error with xml operation", e);
    } catch (IOException e) {
      LOGGER.error("Error with xml operation", e);
    }
    return revisedConfig;
  }

  public double getAggregateMemory(NMProfile profile) {
    double totalTaskMemory;
    double executorMemory = getExecutorMemory();
    double nmJvmMaxMemoryMB = getNodeManagerMemory();
    double advertisableMemory = profile.getMemory();
    totalTaskMemory = executorMemory + nmJvmMaxMemoryMB + advertisableMemory;
    return totalTaskMemory;
  }

  public double getAggregateCpus(NMProfile profile) {
    return getNodeManagerCpus() + MyriadExecutorDefaults.DEFAULT_CPUS + profile.getCpus();
  }

  public double getNodeManagerMemory() {
    NodeManagerConfiguration nmCfg = this.cfg.getNodeManagerConfiguration();
    return (nmCfg.getJvmMaxMemoryMB().isPresent() ? nmCfg.getJvmMaxMemoryMB()
        .get() : NodeManagerConfiguration.DEFAULT_JVM_MAX_MEMORY_MB) * (1 + NodeManagerConfiguration.JVM_OVERHEAD);
  }

  public double getNodeManagerCpus() {
    Optional<Double> cpus = this.cfg.getNodeManagerConfiguration().getCpus();
    return cpus.isPresent() ? cpus.get() : NodeManagerConfiguration.DEFAULT_NM_CPUS;
  }

  public double getExecutorCpus() {

    return MyriadExecutorDefaults.DEFAULT_CPUS;
  }

  public double getExecutorMemory() {
    MyriadExecutorConfiguration executorCfg = this.cfg.getMyriadExecutorConfiguration();
    return (executorCfg.getJvmMaxMemoryMB().isPresent() ? executorCfg.getJvmMaxMemoryMB()
        .get() : MyriadExecutorDefaults.DEFAULT_JVM_MAX_MEMORY_MB) * (1 + MyriadExecutorDefaults.JVM_OVERHEAD);
  }

  public double getTaskCpus(NMProfile profile) {

    return getAggregateCpus(profile) - getExecutorCpus();
  }

  public double getTaskMemory(NMProfile profile) {

    return getAggregateMemory(profile) - getExecutorMemory();
  }

  public double getAuxTaskCpus(NMProfile profile, String taskName) throws MyriadBadConfigurationException {
    if (taskName.startsWith(NodeManagerConfiguration.NM_TASK_PREFIX)) {
      return getAggregateCpus(profile);
    }
    ServiceConfiguration auxConf = cfg.getServiceConfiguration(taskName);
    if (auxConf == null) {
      throw new MyriadBadConfigurationException("Can not find profile for task name: " + taskName);
    }
    if (!auxConf.getCpus().isPresent()) {
      throw new MyriadBadConfigurationException("cpu is not defined for task with name: " + taskName);
    }
    return auxConf.getCpus().get();
  }

  public double getAuxTaskMemory(NMProfile profile, String taskName) throws MyriadBadConfigurationException {
    if (taskName.startsWith(NodeManagerConfiguration.NM_TASK_PREFIX)) {
      return getAggregateMemory(profile);
    }
    ServiceConfiguration auxConf = cfg.getServiceConfiguration(taskName);
    if (auxConf == null) {
      throw new MyriadBadConfigurationException("Can not find profile for task name: " + taskName);
    }
    if (!auxConf.getJvmMaxMemoryMB().isPresent()) {
      throw new MyriadBadConfigurationException("memory is not defined for task with name: " + taskName);
    }
    return auxConf.getJvmMaxMemoryMB().get();
  }

  /**
   * Helper function that returns all scalar resources of a given name in an offer up to a given value.  Attempts to
   * take resource from the prescribed role first and then from the default role.  The variable used indicated any
   * resources previously requested.   Assumes enough resources are present.
   * @param offer - An offer by Mesos, assumed to have enough resources.
   * @param name  - The name of the SCALAR resource, i.e. cpus or mem
   * @param value - The amount of SCALAR resources needed.
   * @param used - The amount of SCALAR resources already removed from this offer.
   * @return An Iterable containing one or two scalar resources of a given name in an offer up to a given value.
   */
  public Iterable<Protos.Resource> getScalarResource(Protos.Offer offer, String name, Double value, Double used) {
    String role = cfg.getFrameworkRole();
    ArrayList<Protos.Resource> resources = new ArrayList<>();
    double resourceDoubleValue = 0;
    //Find role by name, must loop through resources
    for (Protos.Resource r : offer.getResourcesList()) {
      if (r.getName().equals(name) && r.hasRole() && r.getRole().equals(role) && r.hasScalar()) {
        resourceDoubleValue = r.getScalar().getValue();
        if (resourceDoubleValue - used > 0) {
          resources.add(Protos.Resource.newBuilder().setName(name).setType(Protos.Value.Type.SCALAR)
              .setScalar(Protos.Value.Scalar.newBuilder().setValue(Math.min(value, resourceDoubleValue - used)).build())
              .setRole(role).build());
        }
        break;
      } else if (r.getName().equals(name) && r.hasRole() && r.getRole().equals(role)) {
        //Should never get here, there must be a miss configured slave
        LOGGER.warn("Resource with name: " + name + "expected type to be SCALAR check configuration on: " + offer.getHostname());
      }
    }
    //Assume enough resources are present in default value, if not we shouldn't of gotten to this function.
    //Use Math.max in case used>resourceDoubleValue
    if (value - Math.max((resourceDoubleValue - used), 0) > 0) {
      resources.add(Protos.Resource.newBuilder().setName(name).setType(Protos.Value.Type.SCALAR)
          .setScalar(Protos.Value.Scalar.newBuilder().setValue(value - Math.max((resourceDoubleValue - used), 0)).build())
          .build()); //no role assumes default
    }
    return resources;
  }
}
