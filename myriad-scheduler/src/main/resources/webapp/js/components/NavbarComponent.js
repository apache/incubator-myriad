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

import {Navbar, Nav, NavItem, NavDropdown, MenuItem} from 'react-bootstrap';
import {Link} from "react-router-dom";

class NavbarComponent extends React.Component {
    render() {
        return (
            <Navbar fixedTop inverse>

                <Navbar.Header>
                    <Navbar.Brand>
                        <a className="navbar-brand" href="#">
                            <img alt="Myriad Logo" src="/img/navbar_logo.png"/>
                        </a>
                    </Navbar.Brand>
                    <Navbar.Toggle />
                </Navbar.Header>

                <Nav>
                    <NavDropdown id="dropdownMyriad" title="Myriad">
                        <MenuItem>
                            <Link to="frameworkDown">Shutdown Framework</Link>
                        </MenuItem>
                    </NavDropdown>

                    <NavItem>
                        <Link to="flex">Flex</Link>
                    </NavItem>
                    <NavItem>
                        <Link to="tasks">Tasks</Link>
                    </NavItem>

                    <NavDropdown id="dropdownHelp" title="Help">
                        <MenuItem>
                            <Link  to="help">Menu Options</Link>
                        </MenuItem>
                        <MenuItem>
                            <Link  to="about">About Myriad</Link>
                        </MenuItem>
                    </NavDropdown>
                </Nav>

                <Nav pullRight>
                    <NavItem>
                        <Link to="config">Config</Link>
                    </NavItem>
                    <Navbar.Text>{this.props.master}</Navbar.Text>
                </Nav>
            </Navbar>
        );
    }}

export default NavbarComponent;
