/**
 * Copyright 2012-2014 eBay Software Foundation, All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.ebay.myriad.scheduler;

import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.mesos.Protos.CommandInfo;
import org.apache.mesos.Protos.Offer;
import org.apache.mesos.Protos.Resource;
import org.apache.mesos.Protos.TaskID;
import org.apache.mesos.Protos.TaskInfo;
import org.apache.mesos.Protos.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.ebay.myriad.state.NodeTask;

public class TaskUtils {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(TaskUtils.class);

	// TODO(mohit): Make sudo user input driven
	private static final String NM_LAUNCH_CMD = "sudo -H -u hduser bash -c '/usr/local/hadoop/bin/yarn nodemanager'";

	private static final String YARN_NODEMANAGER_RESOURCE_CPU_VCORES = "yarn.nodemanager.resource.cpu-vcores";
	private static final String YARN_NODEMANAGER_RESOURCE_MEMORY_MB = "yarn.nodemanager.resource.memory-mb";

	public static TaskInfo createYARNTask(Offer offer, NodeTask nodeTask) {
		NMProfile profile = nodeTask.getProfile();
		TaskID taskId = TaskID.newBuilder().setValue(nodeTask.getTaskId())
				.build();

		String taskIdValue = taskId.getValue();
		LOGGER.info("Launching task {} with profile: {}", taskIdValue, profile);
		String revisedConfig = getRevisedConfig(profile.getCpus(),
				profile.getMemory());
		String CONFIG_UPDATE_CMD = "sudo -H -u hduser bash -c \"echo '"
				+ revisedConfig
				+ "' > /usr/local/hadoop/etc/hadoop/yarn-site.xml\"; ";

		CommandInfo.Builder commandBuilder = CommandInfo.newBuilder();
		commandBuilder.setUser("root").setValue(
				CONFIG_UPDATE_CMD + NM_LAUNCH_CMD);

		TaskInfo task = TaskInfo
				.newBuilder()
				.setName("task " + taskIdValue)
				.setTaskId(taskId)
				.setSlaveId(offer.getSlaveId())
				.addResources(
						Resource.newBuilder()
								.setName("cpus")
								.setType(Value.Type.SCALAR)
								.setScalar(
										Value.Scalar.newBuilder()
												.setValue(profile.getCpus())
												.build()).build())
				.addResources(
						Resource.newBuilder()
								.setName("mem")
								.setType(Value.Type.SCALAR)
								.setScalar(
										Value.Scalar.newBuilder()
												.setValue(profile.getMemory())
												.build()).build())
				.setCommand(commandBuilder.build()).build();

		return task;
	}

	public static String getRevisedConfig(Double cpu, Double memory) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder;
			Document doc = null;

			builder = factory.newDocumentBuilder();
			InputStream resourceAsStream = TaskUtils.class.getClassLoader()
					.getResourceAsStream("yarn-site.xml");

			doc = builder.parse(new InputSource(resourceAsStream));

			XPathFactory xFactory = XPathFactory.newInstance();

			XPath xpath = xFactory.newXPath();
			XPathExpression cpuXpath = xpath.compile("//property/name");
			Object cpuNodeObj = cpuXpath.evaluate(doc, XPathConstants.NODESET);

			NodeList cpuNode = (NodeList) cpuNodeObj;

			for (int i = 0; i < cpuNode.getLength(); i++) {
				Node item = cpuNode.item(i);
				if (YARN_NODEMANAGER_RESOURCE_CPU_VCORES.equals(item
						.getTextContent())) {
					Node propertyNode = item.getParentNode();
					NodeList childNodes = propertyNode.getChildNodes();
					for (int j = 0; j < childNodes.getLength(); j++) {
						Node item2 = childNodes.item(j);
						if ("value".equals(item2.getNodeName())) {
							item2.setTextContent(cpu.intValue() + "");
						}
					}
				} else if (YARN_NODEMANAGER_RESOURCE_MEMORY_MB.equals(item
						.getTextContent())) {
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
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
					"yes");
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(doc), new StreamResult(writer));
			String output = writer.getBuffer().toString()
					.replaceAll("\n|\r", "");
			return output;
		} catch (Exception e) {
			LOGGER.error("Error with xml operation", e);
		}
		return "";
	}
}
