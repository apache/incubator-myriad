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
<<<<<<< HEAD
    <Navbar fixedTop inverse>
      <Nav>
      <a className="navbar-brand" href="#">
          <img src="/img/navbar_logo.png"></img>
      </a>
=======
    <Navbar fixedTop>
      <Nav bsStyle='tabs'   >
	<DropdownButton  title='Myriad' naveItem={true} >
	   <NavItemLink  to="rmDown">Shutdown RM Only</NavItemLink>
	   <NavItemLink  to="frameworkDown">Shutdown Framework, Stopping Driver</NavItemLink>
	   <NavItemLink  to="gracefulDown">Shutdown Framework, Aborting Driver</NavItemLink>
	</DropdownButton>
>>>>>>> 4721509... Update
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
