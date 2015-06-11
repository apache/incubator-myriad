var React = require("react");
var Myriad = require('./components/Myriad')
var FlexComponent = require('./components/FlexComponent')
var TasksComponent = require('./components/TasksComponent')
var ConfigComponent = require('./components/ConfigComponent')
var AboutComponent = require('./components/AboutComponent')
var HelpComponent = require('./components/HelpComponent')
var ShutdownRmOnlyComponent = require('./components/ShutdownRmOnlyComponent')
var ShutdownFrameworkComponent = require('./components/ShutdownFrameworkComponent')
var ShutdownGracefulComponent = require('./components/ShutdownGracefulComponent')

var Router = require('react-router')
  , RouteHandler= Router.RouteHandler
  , Route = Router.Route
  , Redirect = Router.Redirect;

var routes = (
  <Route name="myriad" path="/" handler={Myriad} >
    <Route name="rmDown" path="rmDown" {...this.props} handler={ShutdownRmOnlyComponent} /> 
    <Route name="frameworkDown" path="frameworkDown" {...this.props} handler={ShutdownFrameworkComponent} /> 
    <Route name="gracefulDown" path="gracefulDown" {...this.props} handler={ShutdownGracefulComponent} /> 
    <Route name="flex" path="flex" handler={FlexComponent} />
    <Route name="tasks" path="tasks" handler={TasksComponent} />
    <Route name="help" path="help" handler={HelpComponent} />
    <Route name="config" path="config" handler={ConfigComponent} />
    <Route name="about" path="/" handler={AboutComponent} />
    <Redirect from="myriad" to="about" />
  </Route>
);

Router.run(routes, function (Handler) {
  React.render(<Handler/>, document.getElementById("myriad"));
});

