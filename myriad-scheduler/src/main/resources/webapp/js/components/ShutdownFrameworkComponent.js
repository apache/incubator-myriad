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

import {Button, Modal} from 'react-bootstrap';

var request = require('superagent');

class ShutdownFrameworkComponent extends React.Component {

	constructor(props, context) {
		super(props, context);

		this.state = {
			isModalOpen: true
		};
	}

	handleToggle() {
		this.setState({
			isModalOpen: !this.state.isModalOpen
		});
	}

	render() {
		return (
			<Modal {...this.props} show={this.state.isModalOpen} bsStyle='primary' id="shutdownModal" title='Shutdown Myriad Framework ?'
				   animation={false} >
				<div className='modal-body' >
					<p> This will stop the driver in failover mode, which will stop the executor and tasks, but not stop the ResourceManager.</p>
				</div>
				<div className='modal-footer'>
					<Button bsStyle="default" onClick={function() {
						this.handleToggle();
						this.onCancel();
					}.bind(this)}>Cancel</Button>
					<Button bsStyle="success" onClick= {function(){
						this.handleToggle();
						this.onRequestShutdown();
					}.bind(this)} >Continue</Button>
				</div>
			</Modal>
		)
	}

	renderOverlay() {
		return <span/>;
	}

	onCancel() {
		this.props.history.push("tasks");
	}

	onRequestShutdown() {
		console.log("shutting down Myriad .... ");
		request.post('/api/framework/shutdown/framework')
			.set('Content-Type', 'application/json')
			.end(function(err, res){
				console.log("Result from /api/framework/shutdown/framework");
				console.log(res);
				if (!err) {
					console.log("Shutdown Myriad framework successful!");
				} else {
					console.log('Shutdown Myriad framework failed: ' + res.text);
				}
			}.bind(this));

		this.props.history.push("tasks");
	}

}

export default ShutdownFrameworkComponent;
