var React = require('react');

var TaskListComponent = React.createClass({
  displayName: "TaskListComponent",

  render: function () {

    var html = [];
    html.push( <h3 key={this.props.name}>{this.props.name}</h3> );
    for( var ii=0; ii < this.props.taskNames.length; ii++ ) {
      html.push(<li key={this.props.taskNames[ii]}>{this.props.taskNames[ii]}</li>);
    }

    return (<ul>{html}</ul>);
   }

});

module.exports = TaskListComponent;
