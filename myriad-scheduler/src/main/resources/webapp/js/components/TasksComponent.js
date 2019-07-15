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

import TaskListComponent from "./TaskListComponent";

import changeCase from 'change-case';

class TasksComponent extends React.Component {

  _prettyName(name) {
    name = changeCase.sentenceCase(name);
    name = changeCase.titleCase(name);
    return name;
  }

  render() {
    var taskTypes = [];
    var keys = [];
    // gather the keys, so task lists can be sorted
    for (var key in this.props.tasks) {
      if (this.props.tasks.hasOwnProperty(key)) {
        keys.push(key);
      }
    }
    keys.sort();
    for (var ii=0; ii<keys.length; ii++) {
      var key = keys[ii];
      taskTypes.push( <TaskListComponent key={key} name={this._prettyName(key)} taskNames={this.props.tasks[key]} /> );
      taskTypes.push( <hr key={'hr_'+key} /> );
    }
    return (<div>{taskTypes}</div>);
  }

}

export default TasksComponent;
