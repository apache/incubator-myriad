var React = require('react');
var NavbarComponent = require('../components/NavbarComponent');

var Router = require('react-router')
  , RouteHandler = Router.RouteHandler;

var request = require('superagent');

var parseString = require('xml2js').parseString;




var Myriad = React.createClass({
  displayName: "Myriad",

  getInitialState: function () {
      return { config: {"profiles":{"small":{"cpu":"1","mem":"1"},
                                    "medium":{"cpu":"2","mem":"2"},
                                    "large":{"cpu":"3","mem":"3"}},
                        "mesosMaster":"127.0.0.1:5050",
                        "restApiPort":"8192"},
               tasks: {"pendingTasks":["pending 1"],
                       "stagingTasks":["staging 1"],
                       "activeTasks":["active 1", "active 2"],
                       "killableTasks":["killable 1"]},
               wadl: "application.wadl not defined.",
               isPolling: false,
               pollInterval: 2000
               }; // Update from store or event.

  },

  fetchState: function () {
    request.get('/api/state')
      .end(function(err, res){
           if (!err) {
             this.setState( {"tasks": res.body});
           } else {
             console.log('Oh no! error on GET api/state ' + res.text);
           }
           if( this.state.isPolling ){
             setTimeout(this.fetchState, this.state.pollInterval);
           }
         }.bind(this));
  },

  fetchConfig: function () {
    request.get('/api/config')
      .end(function(err, res){
           if (!err) {
             this.setState( {"config": res.body});
           } else {
             console.log('Oh no! error on GET api/config ' + res.text);
           }
           if( this.state.isPolling ){
            setTimeout(this.fetchConfig, this.state.pollInterval);
           }
         }.bind(this));
  },


  fetchApi: function () {
    request.get('/api/application.wadl')
      .end(function(err, res){
           if (!err) {
             // the wadl is in XML, xlate to JSON
             parseString(res.text, function(err, json){
                if( !err ) {
                  this.setState({"wadl": json})
                } else {
                  console.log("Error parsing xml to json for .wadl");
                }
             }.bind(this))
           } else {
             console.log('Oh no! error on GET api/application.wadl ' + res.text);
           }
         }.bind(this));
  },

  componentWillMount: function () {
    this.fetchApi();
  },

  componentDidMount: function () {
    // start polling
    this.setState({isPolling: true});
    this.fetchConfig();
    this.fetchState();
  },

  render: function () {

    return (
    <div>
      <NavbarComponent master={this.state.config.mesosMaster}/>
      <div className="container">
        <RouteHandler config={this.state.config} tasks={this.state.tasks} wadl={this.state.wadl} />
      </div>
     </div>
    );
  }
});

module.exports = Myriad;