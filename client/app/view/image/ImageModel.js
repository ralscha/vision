Ext.define('Vision.view.image.ImageModel', {
	extend: 'Ext.app.ViewModel',

	data: {
		selectedImage: null
	},

	stores: {
		images: {
			model: 'Vision.model.Image',
			autoLoad: false,
			remoteSort: false,
			remoteFilter: false,
			pageSize: 0
		}
	}

});
