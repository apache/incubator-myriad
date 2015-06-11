var React = require('react');

var HelpComponent = React.createClass({
  displayName: "HelpComponent",

  render: function () {
    return(
      <div>
	<h4>Myriad dropdown menu</h4>
        <ul>
	   <li>Shutdown RM Only</li>
		<p>Stop only the ResourceManager. The excutors and tasks continue to run.</p>
           <li>Shutdown Framework, Stopping Driver</li>
		<p> This will stop the driver in failover mode, which will stop the executor and tasks, then stop the ResourceManager.
       		This requires the ResourceManager to be either in HA mode or restarted so that it re-registers with Mesos. </p>
           <li>Shutdown Framework, Aborting Driver</li>
 		<p> This will shutdown all the tasks, then send a FrameworkMessage shutdown to the executor, then exits the ResourceManager.
		This allows the ResourceManager to be started backup without requiring it to re-register or be in High Availability (HA) mode, great for testing.</p> 
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
