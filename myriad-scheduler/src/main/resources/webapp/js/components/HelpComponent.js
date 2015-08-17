var React = require('react');

var HelpComponent = React.createClass({
  displayName: "HelpComponent",

  render: function () {
    return(
      <div>
      	<h4>Myriad dropdown menu</h4>
        <ul>
            <li>Shutdown Framework, Stopping Driver</li>
            	<p> This will stop the driver in failover mode, which will stop the executor and tasks, but not stop the ResourceManager.</p>
        </ul>
		<h4>Flex tab</h4>
		<p>Allows you to stand up or stop task resources</p>
		<h4>Tasks tab</h4>
		<p>Allows you to monitor running task states</p>
		<h4>Help menu</h4>
		<ul>
		   <li>Menu options</li>
			<p>This text</p>
		   <li>About Myriad</li>
			<p>Description of Myriad and its profiles</p>
		</ul>
		<h4>Config tab</h4>
		<p>Shows Myriad configuration information</p>
      </div>
   )}

});

module.exports = HelpComponent;
