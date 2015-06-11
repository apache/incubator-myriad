var React = require('react');

var AboutComponent = React.createClass({
  displayName: "AboutComponent",

  render: function () {
    return(
      <div>
        <h1>About</h1>
        <p>
         Myriad allows Mesos and YARN to co-exist and share resources with Mesos
         as the resource manager for the datacenter. Sharing resources between these
          two resource allocation systems improves overall cluster utilization and
          avoids statically partitioning resources amongst two separate clusters/resource managers.
        </p>
        <hr/>
        <h1>API</h1>
        <p>
          Myriad can either be controlled with this user interface or the underlying REST API.
        </p>
        <div>
          <pre>{JSON.stringify(this.props.wadl, null, ' ')}</pre>
        </div>

      </div>
   )}

});

module.exports = AboutComponent;
