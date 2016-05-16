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

var React = require('react');
var ReactBootstrap = require('react-bootstrap')
  , Button = ReactBootstrap.Button
  , Modal = ReactBootstrap.Modal;

var OverlayMixin = ReactBootstrap.OverlayMixin;
var Navigation = require('react-router').Navigation;
var request = require('superagent');

var XModal = React.createClass({
  	mixins: [OverlayMixin],

  	getInitialState: function () {
    		return {
      			isModalOpen: false
    		};
  	},

  	handleToggle: function () {
		// For whatever reason this toggle does not work as claimed on line....so cheat.
		if (!this.state.isModalOpen)
    	    		document.getElementById('shutdownModal').style.visibility = "hidden";
        	this.setState({
            		isModalOpen: !this.state.isModalOpen
        	});
  	},
  	render: function () {
    		return ( <Modal {...this.props} bsStyle='primary'   id="shutdownModal" title='Shutdown Myriad Framework ?' animation={false}  					onRequestHide={this.handleToggle}>
	 			<div className='modal-body' >
					<p> This will stop the driver in failover mode, which will stop the executor and tasks, but not stop the ResourceManager.</p>
				</div>
       				<div className='modal-footer'>
       	    				<Button bsStyle="default" onClick={function() {
						this.handleToggle();
						this.props.onCancel();
						}.bind(this)}>Cancel</Button>
	    				<Button bsStyle="success" onClick= {function(){
                				this.handleToggle();
						this.props.onShutdown();
              					}.bind(this)} >Continue</Button>
	 			</div>
     			</Modal>);
  	},

 	renderOverlay: function () {
      	   		return <span/>;
  	}
});
 
var ShutdownFrameworkComponent = React.createClass({
	mixins: [Navigation],

  	displayName: "ShutdownFrameworkComponent",

  	render: function() {
   		return (
  			<XModal onShutdown={this.onRequestShutdown} onCancel={this.onCancel}/>
    		);
  	},
 	onCancel: function() {
        	this.transitionTo("tasks");
	},
  	onRequestShutdown: function() {
    		console.log( "shutting down Myriad .... ");
    		request.post('/api/framework/shutdown/framework')
    			.set('Content-Type', 'application/json')
    			.end(function(err, res){
           			console.log("Result from /api/framework/shutdown/framework");
           			console.log(res);
		   		if (!err) {
		     			console.log("Shutdown Myraid framework successful!");
		   		} else {
		     			console.log('Shutdown Myraid framework failed: ' + res.text);
           		}
         }.bind(this));

        this.transitionTo("tasks");
      },

});

module.exports = ShutdownFrameworkComponent;
