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
  , Input = ReactBootstrap.Input
  , Button = ReactBootstrap.Button
  , Row = ReactBootstrap.Row
  , Col = ReactBootstrap.Col
  , Label = ReactBootstrap.Label
  , Badge = ReactBootstrap.Badge
  , Modal = ReactBootstrap.Modal
  , ModalTrigger = ReactBootstrap.ModalTrigger;
var Navigation = require('react-router').Navigation;
var request = require('superagent');


var FlexUpModal = React.createClass({

  render: function() {
    return (
      <Modal {...this.props} bsStyle="primary" title='Flex Up Confirmation' animation>
      <Row>
        <Col mdOffset={3}>
          <div className="modal-body">
            Flex up <Badge>{this.props.instances}</Badge> instance(s)
             Profile: <Badge>{this.props.profile}</Badge> ?
          </div>
        </Col>
      </Row>
        <div className="modal-footer">
          <Button bsStyle="default" onClick={this.props.onRequestHide}>Cancel</Button>
          <Button bsStyle="success" onClick={
            function(){
              this.props.onRequestHide();
              this.props.onFlexUp(this.props.instances, this.props.profile);
              }.bind(this) }
          >Flex Up</Button>
        </div>
      </Modal>
    );
  }
});


var FlexUpComponent = React.createClass({
  mixins: [Navigation],

  displayName: "FlexUpComponent",

  getInitialState: function () {
    return( {selectedSProfile: null,
             numInstances:0});
  },

  handleInstanceChange: function() {
    var instances = this.refs.instances.getDOMNode().value;
    this.setState({numInstances: instances});
  },

  handleProfileChange: function() {
    var profile = this.refs.profile.getValue();
    this.setState({selectedProfile: profile});
 },

  componentDidMount: function() {
    this.handleProfileChange();
    this.handleInstanceChange();
  },

  onRequestFlexUp: function(instances, profile) {
    console.log( "flexing up: " + instances + " Profile " + profile);
    request.put('/api/cluster/flexup')
    .set('Content-Type', 'application/json')
    .send({ "profile": profile, "instances": instances})
    .end(function(err, res){
           if (!err) {
             console.log("flexup successful!");
           } else {
             console.log('flexup failed: ' + res.text);
           }
         }.bind(this));

    this.transitionTo("tasks");
  },

  render: function () {

    var options = [];
    var keys = [];
    for( var key in this.props.profiles ) {
      if( this.props.profiles.hasOwnProperty(key) ) {
        keys.push(key);
      }
    }
    for( var ii = 0; ii < keys.length; ii++) {
      var key = keys[ii];
      var txt = key + '\t' + JSON.stringify(this.props.profiles[key]);
      options.push( <option key={key} value={key}>{txt}</option> );
    }

    return(
      <div className="modal-container">
        <Row>
          <Col md={6}>
            <Input type="select" label='Profile' ref="profile" onChange={this.handleProfileChange} >
              { options }
            </Input>
          </Col>
          <Col md={4}>
            <Input label="Instances"
                   help="Enter the number of instances to flex up."
                   wrapperClassName="wrapper"
                   >
                  <input type="number" size="3" defaultValue="1" min="1" max="999" step="1"
                    ref="instances"
                    onChange={this.handleInstanceChange}
                  />
            </Input>
          </Col>
        </Row>
        <Row>
          <Col md={2} mdOffset={5} >
            <ModalTrigger modal={<FlexUpModal
                                    profile={this.state.selectedProfile}
                                    instances={this.state.numInstances}
                                    onFlexUp={this.onRequestFlexUp} />} >
              <Button bsStyle="primary" bsSize="large">Flex Up</Button>
            </ModalTrigger>
          </Col>
        </Row>

      </div>
   )}

  });

module.exports = FlexUpComponent;
