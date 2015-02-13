var React = require('react');


// Renders a json object in a pre

var RawJSONComponent = React.createClass({
  displayName: "RawJSONComponent",

  render: function () {


    return(
      <pre>{JSON.stringify(this.props.json, null, '  ')}</pre>
   )}

});

module.exports = RawJSONComponent;
