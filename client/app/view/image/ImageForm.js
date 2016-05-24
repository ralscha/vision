Ext.define('Vision.view.image.ImageForm', {
	extend: 'Ext.form.Panel',
	reference: 'imageForm',
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
	} ],

	dockedItems: [ {
		xtype: 'toolbar',
		dock: 'top',
		items: [ {
			xtype: 'filefield',
			allowBlank: true,
			reference: 'filefield',
			buttonOnly: true,
			buttonText: 'Select Image...',
			textAlign: 'left',
			listeners: {
				change: 'onImageChange'
			}
		}, {
			text: 'Save',
			iconCls: 'x-fa fa-floppy-o',
			handler: 'onSaveClick',
			formBind: true
		} ]
	} ]

});