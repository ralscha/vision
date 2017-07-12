Ext.define('Vision.Application', {
	extend: 'Ext.app.Application',
	requires: [ 'Ext.plugin.Viewport', 'Ext.direct.*' ],
	name: 'Vision',

    quickTips: false,
    platformConfig: {
        desktop: {
            quickTips: true
        }
    },

	constructor: function() {
		REMOTING_API.maxRetries = 0;
		Ext.direct.Manager.addProvider(REMOTING_API);
		this.callParent(arguments);
	},

	onAppUpdate: function() {
		window.location.reload();
	}
});
