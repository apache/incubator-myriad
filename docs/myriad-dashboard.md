# Myriad Webapp

The Myriad webapp is a [React](http://facebook.github.io/react/) single page application served by Jetty embedded in the myriad jar.

## Building

The app uses [NPM](https://www.npmjs.com/) to manage depencies and [Gulp](http://gulpjs.com/) to assemble the distribution.
The app is served from the webapp/public directory.
To get setup, install `npm` and `gulp` and from the webapp directory execute

```
npm install
gulp
```

## Developing

The gulpfile contains a dev target that launches a node.js webserver and watches the webapp files, re-assembling when
files change. To launch simply run

```
gulp dev
```

A browser window should open with the site loaded. If not, it uses [port 8888](http://localhost:8888)
It is helpful to have myriad setup in Vagrant locally so the api is available. Default values are coded into
the dashboard if Myriad api isn't available.

---
<sub>
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

<sub>
  http://www.apache.org/licenses/LICENSE-2.0

<sub>
Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
