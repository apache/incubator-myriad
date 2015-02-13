var React = require('react');
var ReactBootstrap = require('react-bootstrap')
  , Input = ReactBootstrap.Input
  , Row = ReactBootstrap.Row
  , Col = ReactBootstrap.Col
  , Button = ReactBootstrap.Button
  , Badge = ReactBootstrap.Badge
  , Modal = ReactBootstrap.Modal
  , ModalTrigger = ReactBootstrap.ModalTrigger;
var Navigation = require('react-router').Navigation;
var request = require('superagent');

// some copy pasta from flexup, but they might drift over time
var FlexDownModal = React.createClass({

  render: function() {
    return (
      <Modal {...this.props} bsStyle="primary" title='Flex Down Confirmation' animation>
      <Row>
        <Col mdOffset={3}>
          <div className="modal-body">
            Flex Down <Badge>{this.props.instances}</Badge> instance(s)?
          </div>
        </Col>
      </Row>
        <div className="modal-footer">
          <Button bsStyle="default" onClick={this.props.onRequestHide}>Cancel</Button>
          <Button bsStyle="success" onClick={
            function(){
              this.props.onRequestHide();
              this.props.onFlexDown(this.props.instances);
              }.bind(this) }
          >Flex Down</Button>
        </div>
      </Modal>
    );
  }
});

var FlexDownComponent = React.createClass({
  mixins: [Navigation],

  displayName: "FlexDownComponent",


  getInitialState: function () {
    return( {numInstances:0});
  },

  handleInstanceChange: function() {
    var instances = this.refs.instances.getDOMNode().value;
    this.setState({numInstances: instances});
  },

  componentDidMount: function() {
    this.handleInstanceChange();
  },

  onRequestFlexDown: function(instances, size) {
    console.log( "flexing down: " + instances);
    request.put('/api/cluster/flexdown')
    .set('Content-Type', 'application/json')
    .send({ "instances": instances})
    .end(function(err, res){
           console.log("Result from api/cluster/flexdown");
           console.log(res);
           if (!err) {
             console.log("flexdown successful!");
           } else {
             console.log('flexdown failed: ' + res.text);
           }
         }.bind(this));

    this.transitionTo("tasks");
  },


  render: function () {

    //TODO: get current number of instances available to flex down from the status to set max flex down value
    return(
      <div>
        <Row>
          <Col md={4} mdOffset={1} >
            <Input label="Instances" help="Enter the number of instances to flex down." wrapperClassName="wrapper">
                  <input type="number" size="3" defaultValue="1" min="1" max="999" step="1"
                          ref="instances"
                          onChange={this.handleInstanceChange}/>
            </Input>
          </Col>
        </Row>
        <Row>
          <Col md={2} mdOffset={5} >
            <ModalTrigger modal={<FlexDownModal
                                    instances={this.state.numInstances}
                                    onFlexDown={this.onRequestFlexDown} />} >
              <Button bsStyle="primary" bsSize="large">Flex Down</Button>
            </ModalTrigger>
          </Col>
        </Row>

      </div>
   )}


  });

module.exports = FlexDownComponent;
