var React = require('react');
var FlexUpComponent = require('../components/FlexUpComponent');
var FlexDownComponent = require('../components/FlexDownComponent');

var FlexComponent = React.createClass({
  displayName: "FlexComponent",

  render: function () {
    return(
      <div>
        <h3>Flex Up</h3>
        <FlexUpComponent profiles={this.props.config.profiles} />
        <hr />
        <h3>Flex Down</h3>
        <FlexDownComponent profiles={this.props.config.profiles} />
      </div>

   )}

});

module.exports = FlexComponent;
