Ext.define('Vision.view.image.ImageForm', {
	extend: 'Ext.panel.Panel',

	layout: {
		type: 'vbox',
		align: 'stretch'
	},

	scrollable: true,

	bind: {
		disabled: '{!selectedImage}'
	},

	items: [ {
		xtype: 'container',
		layout: 'fit',
		flex: 1,
		reference: 'canvas',
		items: [ {
			xtype: 'box',
			autoEl: {
				tag: 'canvas'
			}
		} ]
	} ]


});