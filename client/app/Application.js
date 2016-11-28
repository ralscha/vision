Ext.define('Vision.Application', {
	extend: 'Ext.app.Application',
	requires: [ 'Ext.plugin.Viewport', 'Vision.*', 'Ext.direct.*', 'Ext.window.Toast', 
	            'Ext.form.action.DirectSubmit', 'Ext.form.action.DirectLoad', 
	            'Ext.container.Container' ],
	name: 'Vision',

	constructor: function() {
		REMOTING_API.maxRetries = 0;
		Ext.direct.Manager.addProvider(REMOTING_API);
		this.callParent(arguments);
	},

	launch: function() {
	},

	onAppUpdate: function() {
		window.location.reload();
	}
});
