var React = require('react');

var ProfileComponent = React.createClass({
  displayName: "ProfileComponent",

  render: function () {

    return(
      <div>
        <h3>Profile: {this.props.name}</h3>
        <div className="well">
          <h5>CPU: {this.props.profile.cpu}</h5>
          <h5>MEM: {this.props.profile.mem}</h5>
        </div>
      </div>
   )}


  });

module.exports = ProfileComponent;
