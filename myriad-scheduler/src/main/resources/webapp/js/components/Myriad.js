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

import NavbarComponent from '../components/NavbarComponent.js';

import FlexComponent from "./FlexComponent";
import TasksComponent from "./TasksComponent";
import HelpComponent from "./HelpComponent";
import ConfigComponent from "./ConfigComponent";
import AboutComponent from "./AboutComponent";
import ShutdownFrameworkComponent from "./ShutdownFrameworkComponent";

import {Route} from 'react-router-dom';

var request = require('superagent');

var parseString = require('xml2js').parseString;

class Myriad extends React.Component {
    constructor(props, context) {
        super(props, context);

        this.state ={ config: {"profiles":{"small":{"cpu":"1","mem":"1"},
                    "medium":{"cpu":"2","mem":"2"},
                    "large":{"cpu":"3","mem":"3"}},
                "mesosMaster":"127.0.0.1:5050",
                "restApiPort":"8192"},
            tasks: {"pendingTasks":["pending 1"],
                "stagingTasks":["staging 1"],
                "activeTasks":["active 1", "active 2"],
                "killableTasks":["killable 1"]},
            wadl: "application.wadl not defined.",
            isPolling: false,
            pollInterval: 2000
        }; // Update from store or event.

        this.fetchState = this.fetchState.bind(this);
        this.fetchConfig = this.fetchConfig.bind(this);
        this.fetchApi = this.fetchApi.bind(this);
        this.componentWillMount = this.componentWillMount.bind(this);
        this.componentDidMount = this.componentDidMount.bind(this);
    }

    fetchState() {
        request.get('/api/state')
            .end(function(err, res){
                if (!err) {
                    this.setState({"tasks": res.body});
                } else {
                    console.log('Oh no! error on GET api/state ' + res.text);
                }
                if (this.state.isPolling) {
                    setTimeout(this.fetchState, this.state.pollInterval);
                }
            }.bind(this));
    }

    fetchConfig() {
        request.get('/api/config')
            .end(function(err, res){
                if (!err) {
                    this.setState({"config": res.body});
                } else {
                    console.log('Oh no! error on GET api/config ' + res.text);
                }
                if (this.state.isPolling) {
                    setTimeout(this.fetchConfig, this.state.pollInterval);
                }
            }.bind(this));
    }

    fetchApi() {
        request.get('/api/application.wadl')
            .end(function(err, res){
                if (!err) {
                    // the wadl is in XML, xlate to JSON
                    parseString(res.text, function(err, json){
                        if (!err) {
                            this.setState({"wadl": json})
                        } else {
                            console.log("Error parsing xml to json for .wadl");
                        }
                    }.bind(this))
                } else {
                    console.log('Oh no! error on GET api/application.wadl ' + res.text);
                }
            }.bind(this));
    }

    componentWillMount() {
        this.fetchApi();
    }

    componentDidMount() {
        // start polling
        this.setState({isPolling: true});
        this.fetchConfig();
        this.fetchState();
    }

    render() {
        return (
            <div>
                <NavbarComponent master={this.state.config.mesosMaster}/>
                <div className="container">
                    <Route exact path="/frameworkDown"
                           render={(props) => <ShutdownFrameworkComponent
                               {...props} config={this.state.config} tasks={this.state.tasks} wadl={this.state.wadl}/>}
                    />
                    <Route exact path="/flex"
                           render={(props) => <FlexComponent
                               {...props} config={this.state.config} tasks={this.state.tasks} wadl={this.state.wadl}/>}
                    />
                    <Route exact path="/tasks"
                           render={(props) => <TasksComponent
                               {...props} config={this.state.config} tasks={this.state.tasks} wadl={this.state.wadl}/>}
                    />
                    <Route exact path="/help"
                           render={(props) => <HelpComponent
                               {...props} config={this.state.config} tasks={this.state.tasks} wadl={this.state.wadl}/>}
                    />
                    <Route exact path="/config"
                           render={(props) => <ConfigComponent
                               {...props} config={this.state.config} tasks={this.state.tasks} wadl={this.state.wadl}/>}
                    />
                    <Route exact path="/about" component={AboutComponent} />
                </div>
            </div>
        );
    }
}

export default Myriad;
