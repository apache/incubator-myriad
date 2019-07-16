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

class AboutComponent extends React.Component {
    render() {
        return(
            <div>
                <h1>About</h1>
                <p>
                    Myriad allows Mesos and YARN to co-exist and share resources with Mesos
                    as the resource manager for the datacenter. Sharing resources between these
                    two resource allocation systems improves overall cluster utilization and
                    avoids statically partitioning resources amongst two separate clusters/resource managers.
                </p>
                <hr/>
                <h1>API</h1>
                <p>
                    Myriad can either be controlled with this user interface or the underlying REST API.
                </p>
                <div>
                    <pre>{JSON.stringify(this.props.wadl, null, ' ')}</pre>
                </div>

            </div>
        )}

}

export default AboutComponent;
