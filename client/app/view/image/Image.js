Ext.define('Vision.view.image.Image', {
	extend: 'Ext.panel.Panel',

	controller: {
		xclass: 'Vision.view.image.ImageController'
	},

	viewModel: {
		xclass: 'Vision.view.image.ImageModel'
	},

	title: 'Google Vision Demo Application',
	layout: 'hbox',

	items: [ {
		xclass: 'Vision.view.image.ImageGrid',
		flex: 1,
		height: '100%',
		padding: 10
	}, {
		xclass: 'Vision.view.image.ImageForm',
		flex: 3,
		height: '100%',
		padding: 10
	}, {
		xclass: 'Vision.view.image.Vison',
		flex: 2,
		height: '100%',
		padding: 10
	} ]
});