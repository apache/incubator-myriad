var React = require('react');
var ReactBootstrap = require('react-bootstrap')
  , Button = ReactBootstrap.Button
  , Modal = ReactBootstrap.Modal;

var OverlayMixin = ReactBootstrap.OverlayMixin;
var Navigation = require('react-router').Navigation;
var request = require('superagent');

var XModal = React.createClass({
  	mixins: [OverlayMixin],

  	getInitialState() {
    		return {
      			isModalOpen: false
    		};
  	},

  	handleToggle() {
		// For whatever reason this toggle does not work as claimed on line....so cheat.
		if (!this.state.isModalOpen)
    	    		document.getElementById('shutdownModal').style.visibility = "hidden";
        	this.setState({
            		isModalOpen: !this.state.isModalOpen
        	});
  	},
  	render() {
    		return ( <Modal {...this.props} bsStyle='primary'   id="shutdownModal" title='Shutdown Myriad Framework ?' animation={false}  					onRequestHide={this.handleToggle}>
	 			<div className='modal-body' >
					<p> This will stop the driver in failover mode, which will stop the executor and tasks, then stop the ResourceManager.</p>
       					<p> This requires the ResourceManager to be either in HA mode or restarted so that it re-registers with Mesos. </p>
				</div>
       				<div className='modal-footer'>
       	    				<Button bsStyle="default" onClick={function() {
						this.handleToggle();
						this.props.onCancel();
						}.bind(this)}>Cancel</Button>
	    				<Button bsStyle="success" onClick= {function(){
                				this.handleToggle();
						this.props.onShutdown();
              					}.bind(this)} >Continue</Button>
	 			</div>
     			</Modal>);
  	},

 	renderOverlay() {
      	   		return <span/>;
  	}
});
 
var ShutdownFrameworkComponent = React.createClass({
	mixins: [Navigation],

  	displayName: "ShutdownFrameworkComponent",

  	render() {
   		return (
  			<XModal onShutdown={this.onRequestShutdown} onCancel={this.onCancel}/>
    		);
  	},
 	onCancel : function() {
        	this.transitionTo("tasks");
	},
  	onRequestShutdown: function() {
    		console.log( "shutting down Myriad .... ");
    		request.get('/api/framework/shutdown/framework')
    			.set('Content-Type', 'application/json')
    			.end(function(err, res){
           			console.log("Result from /api/framework/shutdown/framework");
           			console.log(res);
		   		if (!err) {
		     			console.log("Shutdown Myraid framework successful!");
		   		} else {
		     			console.log('Shutdown Myraid framework failed: ' + res.text);
           		}
         }.bind(this));

        this.transitionTo("tasks");
      },

});

module.exports = ShutdownFrameworkComponent;
