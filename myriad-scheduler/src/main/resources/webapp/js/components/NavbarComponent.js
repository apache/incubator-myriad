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
  , Nav = ReactBootstrap.Nav
  , Navbar = ReactBootstrap.Navbar
  , DropdownButton = ReactBootstrap.DropdownButton
  , MenuItem = ReactBootstrap.MenuItem
  , Label = ReactBootstrap.Label;

var ReactRouterBootstrap = require('react-router-bootstrap')
  , NavItemLink = ReactRouterBootstrap.NavItemLink
  , ButtonLink = ReactRouterBootstrap.ButtonLink;


var NavbarComponent = React.createClass({
  name: "NavbarComponent",

  render: function () {
    return(
    <Navbar fixedTop inverse>
      <Nav>
      <a className="navbar-brand" href="#">
          <img src="/img/navbar_logo.png"></img>
      </a>
      </Nav>
      <Nav bsStyle='tabs'   >
      		<DropdownButton  title='Myriad' naveItem={true} >
      			<NavItemLink  to="frameworkDown">Shutdown Framework</NavItemLink>
 	   		</DropdownButton>
 	  </Nav>
      <Nav>
        <NavItemLink to="flex">Flex</NavItemLink>
        <NavItemLink to="tasks">Tasks</NavItemLink>
      </Nav>
      <Nav bsStyle='tabs'   >
      	<DropdownButton  title='Help' naveItem={true} >
      		<NavItemLink  to="help">Menu Options</NavItemLink>
      		<NavItemLink  to="about">About Myriad</NavItemLink>
      	</DropdownButton>
      </Nav>

      <Nav right>
        <NavItemLink to="config">Config</NavItemLink>
        <span className="navbar-text">
          <Label bsStyle="default">{this.props.master}</Label>
        </span>
      </Nav>

    </Navbar>
   )}

});

module.exports = NavbarComponent;
