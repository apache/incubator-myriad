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
import ReactDOM from "react-dom";

import {Button, FormControl, Row, Col, Badge, Modal} from 'react-bootstrap';
import {withRouter} from 'react-router-dom';

var request = require('superagent');

class FlexDownComponent extends React.Component {

    constructor(props, context) {
        super(props, context);

        this.state ={selectedProfile: null,
            numInstances:0, show:false};

        this.handleShow = this.handleShow.bind(this);
        this.handleClose = this.handleClose.bind(this);
        this.handleInstanceChange = this.handleInstanceChange.bind(this);
        this.handleProfileChange = this.handleProfileChange.bind(this);
        this.componentDidMount = this.componentDidMount.bind(this);
    }

    handleInstanceChange() {
        var instances = ReactDOM.findDOMNode(this.refs.instances).value;
        this.setState({numInstances: instances});
    }

    handleProfileChange() {
        var profile = ReactDOM.findDOMNode(this.refs.profile).value;
        this.setState({selectedProfile: profile});
    }

    componentDidMount() {
        this.handleProfileChange();
        this.handleInstanceChange();
    }

    onRequestFlexDown(instances, profile, constraint) {
        console.log("flexing down: " + instances + " profile: " +  profile);
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

        this.props.history.push("tasks");
    }

    handleClose() {
        this.setState({ show: false });
    }

    handleShow() {
        this.setState({ show: true });
    }

    render() {
        var options = [];
        var keys = [];
        for (var key in this.props.profiles) {
            if (this.props.profiles.hasOwnProperty(key)) {
                keys.push(key);
            }
        }
        for (var ii = 0; ii < keys.length; ii++) {
            var key = keys[ii];
            var txt = key + '\t' + JSON.stringify(this.props.profiles[key]);
            options.push( <option key={key} value={key}>{txt}</option> );
        }

        //TODO: get current number of instances available to flex down from the status to set max flex down value
        return(
            <div>
                <Row>
                    <Col md={6}>
                        <FormControl componentClass="select" placeholder="select" label='Profile' ref="profile"
                                     onChange={this.handleProfileChange}>
                            { options }
                        </FormControl>
                    </Col>
                    <Col md={4} >
                        <FormControl
                            type="number"
                            size="3" defaultValue="1" min="1" max="999" step="1"
                            placeholder="instances"
                            ref="instances"
                            onChange={this.handleInstanceChange}
                        />
                    </Col>
                </Row>
                <Row>
                    <Col md={2} mdOffset={5} >
                        <Button bsStyle="primary" bsSize="large" onClick={this.handleShow}>
                            Flex Down
                        </Button>
                        <Modal show={this.state.show} bsStyle="primary" title='Flex Down Confirmation' animation>
                            <Row>
                                <Col mdOffset={3}>
                                    <div className="modal-body">
                                        Flex Down <Badge>{this.state.numInstances}</Badge> instance(s)
                                        Profile: <Badge>{this.state.selectedProfile}</Badge> ?
                                    </div>
                                </Col>
                            </Row>
                            <div className="modal-footer">
                                <Button bsStyle="default" onClick={this.handleClose}>Cancel</Button>
                                <Button bsStyle="success" onClick={
                                    function(){
                                        this.handleClose();
                                        this.onRequestFlexDown(this.state.numInstances, this.state.selectedProfile);
                                    }.bind(this) }
                                >Flex Down</Button>
                            </div>
                        </Modal>
                    </Col>
                </Row>
            </div>
        )}
}

export default withRouter(FlexDownComponent);
