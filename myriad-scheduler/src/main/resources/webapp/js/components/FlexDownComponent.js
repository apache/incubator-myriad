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
  , Row = ReactBootstrap.Row
  , Col = ReactBootstrap.Col
  , Button = ReactBootstrap.Button
  , Badge = ReactBootstrap.Badge
  , Modal = ReactBootstrap.Modal
  , ModalTrigger = ReactBootstrap.ModalTrigger;
var Navigation = require('react-router').Navigation;
var request = require('superagent');

// some copy pasta from flexup, but they might drift over time
var FlexDownModal = React.createClass({

  render: function() {

    return (
      <Modal {...this.props} bsStyle="primary" title='Flex Down Confirmation' animation>
      <Row>
        <Col mdOffset={3}>
          <div className="modal-body">
            Flex Down <Badge>{this.props.instances}</Badge> instance(s)
            Profile: <Badge>{this.props.profile}</Badge> ?
          </div>
        </Col>
      </Row>
        <div className="modal-footer">
          <Button bsStyle="default" onClick={this.props.onRequestHide}>Cancel</Button>
          <Button bsStyle="success" onClick={
            function(){
              this.props.onRequestHide();
              this.props.onFlexDown(this.props.instances, this.props.profile);
              }.bind(this) }
          >Flex Down</Button>
        </div>
      </Modal>
    );
  }
});

var FlexDownComponent = React.createClass({
  mixins: [Navigation],

  displayName: "FlexDownComponent",


  getInitialState: function () {
    return( {selectedProfile: null,
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

  onRequestFlexDown: function(instances, profile, constraint) {
    console.log( "flexing down: " + instances + " profile: " +  profile);
    request.put('/api/cluster/flexdown')
    .set('Content-Type', 'application/json')
    .send({ "profile": profile, "instances": instances})
    .end(function(err, res){
           console.log("Result from api/cluster/flexdown");
           console.log(res);
           if (!err) {
             console.log("flexdown successful!");
           } else {
             console.log('flexdown failed: ' + res.text);
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

    //TODO: get current number of instances available to flex down from the status to set max flex down value
    return(
      <div>
        <Row>
        <Col md={6}>
                    <Input type="select" label='Profile' ref="profile" onChange={this.handleProfileChange} >
                      { options }
                    </Input>
         </Col>
          <Col md={4} >
            <Input label="Instances" help="Enter the number of instances to flex down." wrapperClassName="wrapper">
                  <input type="number" size="3" defaultValue="1" min="1" max="999" step="1"
                          ref="instances"
                          onChange={this.handleInstanceChange}/>
            </Input>
          </Col>
        </Row>
        <Row>
          <Col md={2} mdOffset={5} >
            <ModalTrigger modal={<FlexDownModal
                                    profile={this.state.selectedProfile}
                                    instances={this.state.numInstances}
                                    onFlexDown={this.onRequestFlexDown} />} >
              <Button bsStyle="primary" bsSize="large">Flex Down</Button>
            </ModalTrigger>
          </Col>
        </Row>

      </div>
   )}


  });

module.exports = FlexDownComponent;
