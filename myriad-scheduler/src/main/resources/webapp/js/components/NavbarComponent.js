var React = require('react');

var ReactBootstrap = require('react-bootstrap')
  , Nav = ReactBootstrap.Nav
  , Navbar = ReactBootstrap.Navbar
  , Label = ReactBootstrap.Label;

var ReactRouterBootstrap = require('react-router-bootstrap')
  , NavItemLink = ReactRouterBootstrap.NavItemLink
  , ButtonLink = ReactRouterBootstrap.ButtonLink;


var NavbarComponent = React.createClass({
  name: "NavbarComponent",

  render: function () {
    return(
    <Navbar fixedTop>
      <a className="navbar-brand" href="#">Myriad</a>
      <Nav>
        <NavItemLink to="flex">Flex</NavItemLink>
        <NavItemLink to="tasks">Tasks</NavItemLink>
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
