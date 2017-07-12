Ext.define('Vision.view.image.Vison', {
	extend: 'Ext.tab.Panel',

	bind: {
		disabled: '{!selectedImage}'
	},

	items: [ {
		xtype: 'grid',
		title: 'Labels',
		viewConfig: {
			stripeRows: false
		},
		bind: {
			store: '{selectedImage.labels}'
		},
		columns: [ {
			text: 'Description',
			dataIndex: 'description',
			flex: 1,
			menuDisabled: true
		}, {
			text: 'Score',
			dataIndex: 'score',
			width: 60,
			menuDisabled: true,
			xtype: 'widgetcolumn',
			widget: {
				xtype: 'progressbarwidget',
				textTpl: [ '{value:percent("0")}' ]
			}
		} ]
	}, {
		xtype: 'panel',
		title: 'Web',
		layout: {
			type: 'vbox',
			align: 'stretch'
		},
		items: [ {
			xtype: 'grid',
			flex: 1,
			viewConfig: {
				stripeRows: false
			},
			bind: {
				store: '{selectedImage.web.webEntities}'
			},
			columns: [ {
				text: 'Description',
				dataIndex: 'description',
				flex: 1,
				menuDisabled: true
			}, {
				text: 'Score',
				dataIndex: 'score',
				flex: 1,
				menuDisabled: true
			} ]
		}, {
			xtype: 'grid',
			title: 'Full Matching Images',
			flex: 1,
			viewConfig: {
				stripeRows: false
			},
			bind: {
				store: '{selectedImage.web.fullMatchingImages}'
			},
			columns: [ {
				text: 'URL',
				dataIndex: 'url',
				flex: 1,
				menuDisabled: true,
				renderer: function(value) {
				    return value.replace(/(https?:\/\/\S+)/g, '<a target="_blank" href="$1">$1</a>');
				}
			} ]
		}, {
			xtype: 'grid',
			title: 'Partial Matching Images',
			flex: 1,
			viewConfig: {
				stripeRows: false
			},
			bind: {
				store: '{selectedImage.web.partialMatchingImages}'
			},
			columns: [ {
				text: 'URL',
				dataIndex: 'url',
				flex: 1,
				menuDisabled: true,
				renderer: function(value) {
				    return value.replace(/(https?:\/\/\S+)/g, '<a target="_blank" href="$1">$1</a>');
				}
			} ]
		}, {
			xtype: 'grid',
			title: 'Pages With Matching Images',
			flex: 1,
			viewConfig: {
				stripeRows: false
			},
			bind: {
				store: '{selectedImage.web.pagesWithMatchingImages}'
			},
			columns: [ {
				text: 'URL',
				dataIndex: 'url',
				flex: 1,
				menuDisabled: true,
				renderer: function(value) {
				    return value.replace(/(https?:\/\/\S+)/g, '<a target="_blank" href="$1">$1</a>');
				}
			} ]
		} ]
	}, {
		xtype: 'panel',
		title: 'Faces',
		layout: {
			type: 'vbox',
			align: 'stretch'
		},
		items: [ {
			xtype: 'grid',
			flex: 1,
			reference: 'facesGrid',

			viewConfig: {
				stripeRows: false,
				listeners: {
					cellclick: 'onCellClick'
				}
			},
			bind: {
				store: '{selectedImage.faces}'
			},
			listeners: {
				selectionchange: 'onFaceClick'
			},
			columns: [ {
				text: 'Joy',
				dataIndex: 'joyRating',
				width: 60,
				menuDisabled: true,
				xtype: 'widgetcolumn',
				widget: {
					xtype: 'progressbarwidget',
					textTpl: [ '{value}' ]
				}
			}, {
				text: 'Sorrow',
				dataIndex: 'sorrowRating',
				width: 70,
				menuDisabled: true,
				xtype: 'widgetcolumn',
				widget: {
					xtype: 'progressbarwidget',
					textTpl: [ '{value}' ]
				}
			}, {
				text: 'Anger',
				dataIndex: 'angerRating',
				width: 60,
				menuDisabled: true,
				xtype: 'widgetcolumn',
				widget: {
					xtype: 'progressbarwidget',
					textTpl: [ '{value}' ]
				}
			}, {
				text: 'Suprise',
				dataIndex: 'surpriseRating',
				width: 70,
				menuDisabled: true,
				xtype: 'widgetcolumn',
				widget: {
					xtype: 'progressbarwidget',
					textTpl: [ '{value}' ]
				}
			}, {
				text: 'Headwear',
				dataIndex: 'headwearRating',
				width: 85,
				menuDisabled: true,
				xtype: 'widgetcolumn',
				widget: {
					xtype: 'progressbarwidget',
					textTpl: [ '{value}' ]
				}
			}, {
				text: 'Confidence',
				dataIndex: 'detectionConfidence',
				width: 120,
				menuDisabled: true,
				xtype: 'widgetcolumn',
				widget: {
					xtype: 'progressbarwidget',
					textTpl: [ '{value:percent("0")}' ]
				}
			} ]
		}, {
			xtype: 'grid',
			flex: 1,
			reference: 'faceLandmarksGrid',
			viewConfig: {
				stripeRows: false
			},
			listeners: {
				selectionchange: 'onFaceLandmarkClick'
			},
			columns: [ {
				text: 'Face Landmarks',
				dataIndex: 'type',
				flex: 1,
				menuDisabled: true
			} ]
		} ]
	}, {
		xtype: 'panel',
		title: 'Landmarks',
		layout: {
			type: 'vbox',
			align: 'stretch'
		},
		items: [ {
			flex: 1,
			xtype: 'grid',
			viewConfig: {
				stripeRows: false
			},
			bind: {
				store: '{selectedImage.landmarks}'
			},
			listeners: {
				selectionchange: 'onBoundingPolyClick'
			},
			columns: [ {
				text: 'Description',
				dataIndex: 'description',
				flex: 1,
				menuDisabled: true
			}, {
				text: 'Score',
				dataIndex: 'score',
				width: 60,
				menuDisabled: true,
				xtype: 'widgetcolumn',
				widget: {
					xtype: 'progressbarwidget',
					textTpl: [ '{value:percent("0")}' ]
				}
			} ]
		}, {
			flex: 2,
			xclass: 'Vision.view.image.GMapPanel',
			reference: 'googleMap',
			gmapType: 'map',
			center: {
				lat: 36.53387,
				lng: -22.71878
			},
			mapOptions: {
				mapTypeId: google.maps.MapTypeId.ROADMAP,
				zoom: 2
			}
		} ]
	}, {
		xtype: 'grid',
		title: 'Logos',
		viewConfig: {
			stripeRows: false
		},
		bind: {
			store: '{selectedImage.logos}'
		},
		listeners: {
			selectionchange: 'onBoundingPolyClick'
		},
		columns: [ {
			text: 'Description',
			dataIndex: 'description',
			flex: 1,
			menuDisabled: true
		}, {
			text: 'Score',
			dataIndex: 'score',
			width: 60,
			menuDisabled: true,
			xtype: 'widgetcolumn',
			widget: {
				xtype: 'progressbarwidget',
				textTpl: [ '{value:percent("0")}' ]
			}
		} ]

	}, {
		xtype: 'grid',
		title: 'Text',
		viewConfig: {
			stripeRows: false
		},
		bind: {
			store: '{selectedImage.texts}'
		},
		listeners: {
			selectionchange: 'onBoundingPolyClick'
		},
		columns: [ {
			text: 'Description',
			dataIndex: 'description',
			flex: 1,
			menuDisabled: true
		} ]

	}, {
		xtype: 'form',
		bodyPadding: 10,
		title: 'Safe Search',
		items: [ {
			xtype: 'fieldcontainer',
			fieldLabel: 'Adult',
			labelWidth: 60,
			layout: 'hbox',
			items: [ {
				xtype: 'progressbarwidget',
				textTpl: [ '{value}' ],
				width: 60,
				bind: '{selectedImage.safeSearch.adultRating}'
			}, {
				xtype: 'displayfield',
				padding: '0 0 0 10',
				bind: '{selectedImage.safeSearch.adult}'
			} ]
		}, {
			xtype: 'fieldcontainer',
			fieldLabel: 'Spoof',
			labelWidth: 60,
			layout: 'hbox',
			items: [ {
				xtype: 'progressbarwidget',
				textTpl: [ '{value}' ],
				width: 60,
				bind: '{selectedImage.safeSearch.spoofRating}'
			}, {
				xtype: 'displayfield',
				padding: '0 0 0 10',
				bind: '{selectedImage.safeSearch.spoof}'
			} ]
		}, {
			xtype: 'fieldcontainer',
			fieldLabel: 'Medical',
			labelWidth: 60,
			layout: 'hbox',
			items: [ {
				xtype: 'progressbarwidget',
				textTpl: [ '{value}' ],
				width: 60,
				bind: '{selectedImage.safeSearch.medicalRating}'
			}, {
				xtype: 'displayfield',
				padding: '0 0 0 10',
				bind: '{selectedImage.safeSearch.medical}'
			} ]
		}, {
			xtype: 'fieldcontainer',
			fieldLabel: 'Violence',
			labelWidth: 60,
			layout: 'hbox',
			items: [ {
				xtype: 'progressbarwidget',
				textTpl: [ '{value}' ],
				width: 60,
				bind: '{selectedImage.safeSearch.violenceRating}'
			}, {
				xtype: 'displayfield',
				padding: '0 0 0 10',
				bind: '{selectedImage.safeSearch.violence}'
			} ]
		} ]
	} ]
});