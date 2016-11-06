Ext.define("Vision.EventBus", {
	requires: [ 'Ext.data.identifier.Uuid' ],
	singleton: true,

	constructor: function() {
		this.id = Ext.data.identifier.Uuid.create().generate();
	},

	start: function(callback) {
		this.eventSource = new EventSource('eventbus/' + this.id);
		if (callback) {
			this.eventSource.addEventListener('open', callback);
		}
	},

	stop: function() {
		Ext.Ajax.request({
			url: 'eventbus/unsubscribe/' + this.id
		})
		.then()
		.always(function() {
			if (this.eventSource) {
				this.eventSource.close();
				this.eventSource = null;
			}
		}, this);

	},

	subscribe: function(eventName, listener) {
		Ext.Ajax.request({
			url: 'eventbus/subscribe/' + this.id + '/' + eventName
		})
		.then(function() {
			this.eventSource.addEventListener(eventName, listener, false);
		}, this);
	},

	unsubscribe: function(eventName, listener) {
		Ext.Ajax.request({
			url: 'eventbus/unsubscribe/' + this.id + '/' + eventName
		})
		.then()
		.always(function() {
			this.eventSource.removeEventListener(eventName, listener, false);
		}, this);
	}
});