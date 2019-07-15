/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import React from 'react';

class HelpComponent extends React.Component {

	render() {
		return(
			<div>
				<h4>Myriad dropdown menu</h4>
				<ul>
					<li>Shutdown Framework, Stopping Driver</li>
					<p> This will stop the driver in failover mode, which will stop the executor and tasks, but not stop the ResourceManager.</p>
				</ul>
				<h4>Flex tab</h4>
				<p>Allows you to stand up or stop task resources</p>
				<h4>Tasks tab</h4>
				<p>Allows you to monitor running task states</p>
				<h4>Help menu</h4>
				<ul>
					<li>Menu options</li>
					<p>This text</p>
					<li>About Myriad</li>
					<p>Description of Myriad and its profiles</p>
				</ul>
				<h4>Config tab</h4>
				<p>Shows Myriad configuration information</p>
			</div>
		)}

}

export default HelpComponent;