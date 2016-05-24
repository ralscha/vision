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

	columns: [ {
		text: 'Thumbnail',
		menuDisabled: true,
		flex: 1,
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
			text: 'New',
			iconCls: 'x-fa fa-plus',
			handler: 'onNewClick'
		}, {
			text: 'Delete',
			iconCls: 'x-fa fa-trash',
			handler: 'onDeleteClick',
			bind: {
				disabled: '{!selectedImage}'
			}
		} ]
	} ]

});