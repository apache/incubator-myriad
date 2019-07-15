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

import ProfileComponent from "./ProfileComponent";
import RawJSONComponent from "./RawJSONComponent";

class ConfigComponent extends React.Component {

  render() {

    // get all the profile names from the config
    var profileNames = [];
    for (var key in this.props.config.profiles) {
      if (this.props.config.profiles.hasOwnProperty(key)) {
        profileNames.push(key);
      }
    }

    var html = [];
    for (var ii=0; ii<profileNames.length; ii++) {
      var name = profileNames[ii];
      html.push(<ProfileComponent key={name} name={name} profile={this.props.config.profiles[name]}/>);
      html.push(<hr key={'hr_'+name} />);
    }

    return(
        <div>
          {html}
          <h3>Raw Json</h3>
          <RawJSONComponent json={this.props.config}/>
        </div>
    )}

}

export default ConfigComponent;
