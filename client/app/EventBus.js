Ext.define("Vision.EventBus", {
	requires: [ 'Ext.data.identifier.Uuid' ],
	singleton: true,

	constructor: function() {
		this.id = Ext.data.identifier.Uuid.create().generate();
	},

	start: function() {
		this.eventSource = new EventSource('eventbus/' + this.id);
	},

	subscribe: function(eventName, listener) {
		this.eventSource.addEventListener(eventName, listener, false);
	}
});