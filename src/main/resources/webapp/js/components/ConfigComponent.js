var React = require('react');
var ProfileComponent = require('../components/ProfileComponent');
var RawJSONComponent = require('../components/RawJSONComponent');

var ConfigComponent = React.createClass({
  displayName: "ConfigComponent",

  render: function () {

    // get all the profile names from the config
    var profileNames = [];
    for(var key in this.props.config.profiles) {
      if (this.props.config.profiles.hasOwnProperty(key)) {
        profileNames.push(key);
      }
    }

    var html = [];
    for( var ii=0; ii<profileNames.length; ii++) {
      var name = profileNames[ii];
      html.push( <ProfileComponent key={name} name={name} profile={this.props.config.profiles[name]} /> );
      html.push( <hr key={'hr_'+name} />);
    }


    return(
      <div>
        {html}
        <h3>Raw Json</h3>
        <RawJSONComponent json={this.props.config} />
      </div>
   )}

});

module.exports = ConfigComponent;
