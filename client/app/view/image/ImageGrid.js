Ext.define('Vision.view.image.ImageGrid', {
	extend: 'Ext.grid.Panel',

	autoLoad: true,

	bind: {
		store: '{images}',
		selection: '{selectedImage}'
	},

	listeners: {
		selectionchange: 'onSelectionChange'
	},

	viewConfig: {
		stripeRows: false
	},

	hideHeaders: true,

	columns: [ {
		menuDisabled: true,
		flex: 1,
		align: 'center',
		dataIndex: 'thumbnail',
		renderer: function(value) {
			if (value) {
				return '<img src="' + value + '" width="50">';
			}
			return value;
		}
	} ],

	dockedItems: [ {
		xtype: 'toolbar',
		dock: 'top',
		items: [ {
			xtype: 'filefield',
			allowBlank: true,
			reference: 'filefield',
			buttonOnly: true,
			buttonText: 'Upload New Image...',
			textAlign: 'left',
			listeners: {
				change: 'onImageChange'
			}
		} ]
	}, {
		xtype: 'toolbar',
		dock: 'bottom',
		items: [ {
			text: 'Delete',
			iconCls: 'x-fa fa-trash',
			handler: 'onDeleteClick',
			bind: {
				disabled: '{!selectedImage}'
			}
		} ]
	} ]

});