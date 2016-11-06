Ext.define('Vision.view.image.ImageController', {
	extend: 'Ext.app.ViewController',

	init: function() {
		var me = this;
		Vision.EventBus.start(function() {
			Vision.EventBus.subscribe("imageadded", me.onImageAdded.bind(me));
		});
	},

	onImageAdded: function(event) {
		var imageJson = JSON.parse(event.data);
		var newImage = Vision.model.Image.create(imageJson);
		this.getStore('images').insert(0, newImage);
		this.updateStores(newImage);
	},

	getCanvas: function() {
		if (!this.canvas) {
			var canvasContainer = this.lookup('canvas');
			this.canvas = canvasContainer.items.items[0].el.dom;
			this.canvas.width = canvasContainer.getWidth();
			this.canvas.height = canvasContainer.getHeight();
		}
		return this.canvas;
	},

	get2dContext: function() {
		return this.getCanvas().getContext("2d");
	},

	onCellClick: function(table, td, cellIndex, record) {
		this.lookup('facesGrid').setSelection(record);
	},

	onSelectionChange: function(grid, selected) {
		if (selected && selected.length > 0) {
			var record = selected[0];

			var gmap = this.lookup('googleMap');
			gmap.deleteMarkers();
			gmap.setCenter({
				lat: 36.53387,
				lng: -22.71878
			});
			if (gmap.gmap) {
				gmap.gmap.setZoom(2);
			}

			if (!record.phantom) {
				var canvas = this.getCanvas();

				var img = new Image();
				img.src = 'image/' + record.getId() + '/' + record.get('name');
				img.onload = this.drawImageScaled.bind(this, img);
			}

			var faceLandmarksGrid = this.lookup('faceLandmarksGrid');
			faceLandmarksGrid.getStore().removeAll();
		}
	},

	onImageChange: function(tf) {		
		var newImage = new Vision.model.Image();
		this.getViewModel().set('selectedImage', newImage);
		this.clearImage();
		
		var me = this;
		var file = tf.fileInputEl.dom.files[0];

		var vm = this.getViewModel();
		var image = vm.get('selectedImage');

		image.set('name', file.name);
		image.set('size', file.size);
		image.set('type', file.type);

		var fileReader = new FileReader();
		fileReader.fileName = file.name;
		fileReader.onload = function(fileLoadedEvent) {
			var imageData = fileLoadedEvent.target.result;

			var img = new Image();
			img.src = imageData;

			var canvas = me.getCanvas();

			me.drawImageScaled(img);
			image.set('data', imageData);
			tf.reset();

			me.save();
		};
		fileReader.readAsDataURL(file);

	},

	clearImage: function() {
		var canvas = this.getCanvas();
		var ctx = this.get2dContext();
		ctx.clearRect(0, 0, canvas.width, canvas.height);
	},

	redrawImage: function() {
		this.clearImage();
		if (this.image) {
			this.drawImageScaled(this.image);
		}
	},

	drawVertices: function(vertices) {
		if (vertices) {
			this.redrawImage();

			var ctx = this.get2dContext();
			ctx.beginPath();
			ctx.moveTo(vertices[0].x * this.ratio, vertices[0].y * this.ratio);
			ctx.lineTo(vertices[1].x * this.ratio, vertices[1].y * this.ratio);
			ctx.lineTo(vertices[2].x * this.ratio, vertices[2].y * this.ratio);
			ctx.lineTo(vertices[3].x * this.ratio, vertices[3].y * this.ratio);
			ctx.closePath();
			ctx.strokeStyle = "#bada55";
			ctx.lineWidth = 4;
			ctx.stroke();
		}
	},

	onBoundingPolyClick: function(grid, selected) {
		if (selected && selected.length > 0) {
			var record = selected[0];
			this.drawVertices(record.get('boundingPoly'));

			var locations = record.get('locations');
			if (locations) {
				var gmap = this.lookup('googleMap');
				gmap.deleteMarkers();

				var boundary = new google.maps.LatLngBounds();

				for (var i = 0; i < locations.length; i++) {
					var markerLatLng = new google.maps.LatLng(locations[i].lat, locations[i].lng);
					boundary.extend(markerLatLng);
					gmap.addMarker({
						position: markerLatLng,
						title: record.get('description')
					});
				}

				if (!boundary.isEmpty()) {
					gmap.setCenter(boundary.getCenter());
					gmap.fitBounds(boundary);
					gmap.gmap.setZoom(11);
				}
				else {
					gmap.setCenter({
						lat: 36.53387,
						lng: -22.71878
					});
					gmap.gmap.setZoom(2);
				}
			}
		}
	},

	onFaceClick: function(grid, selected) {
		if (selected && selected.length > 0) {
			var record = selected[0];
			this.selectedFacePoly = record.get('boundingPoly');
			this.drawVertices(this.selectedFacePoly);

			var faceLandmarksGrid = this.lookup('faceLandmarksGrid');
			var store = Ext.create('Ext.data.Store');
			store.loadData(record.get('landmarks'));
			faceLandmarksGrid.setStore(store);
		}
	},

	onFaceLandmarkClick: function(grid, selected) {
		if (selected && selected.length > 0) {
			var record = selected[0];

			this.redrawImage();
			if (this.selectedFacePoly) {
				this.drawVertices(this.selectedFacePoly);
			}

			var ctx = this.get2dContext();

			var x = record.get('x') * this.ratio;
			var y = record.get('y') * this.ratio;

			ctx.beginPath();
			ctx.arc(x, y, 5, 0, 2 * Math.PI, false);
			ctx.lineWidth = 3;
			ctx.strokeStyle = '#bada55';
			ctx.stroke();
		}
	},

	drawImageScaled: function(img) {
		var canvas = this.getCanvas();
		var ctx = this.get2dContext();

		var hRatio = canvas.width / img.width;
		var vRatio = canvas.height / img.height;
		this.ratio = Math.min(hRatio, vRatio);
		if (this.ratio > 1) {
			this.ratio = 1;
		}

		// var centerShift_x = (canvas.width - img.width * this.ratio) / 2;
		// var centerShift_y = (canvas.height - img.height * this.ratio) / 2;
		ctx.clearRect(0, 0, canvas.width, canvas.height);
		ctx.drawImage(img, 0, 0, img.width, img.height, 0/* centerShift_x */, 0/* centerShift_y */, img.width
				* this.ratio, img.height * this.ratio);

		this.image = img;
	},

	onDeleteClick: function() {
		var image = this.getViewModel().get('selectedImage');

		Ext.Msg.confirm('Attention', 'Do you really want to delete the picture?', function(choice) {
			if (choice === 'yes') {
				imageController.destroy(image.getId(), function() {
					this.getViewModel().set('selectedImage', null);
					this.getStore('images').remove(image);
					this.image = null;
					this.clearImage();
				}, this);
			}
		}, this);
	},

	save: function() {
		var image = this.getViewModel().get('selectedImage');
		this.getView().mask('Uploading to the Server...');
		image.save({
			success: function(record) {
				this.getStore('images').add(record);

				this.updateStores(record);
			},
			callback: function(record, operation, success) {
				this.getView().unmask();
			},
			scope: this
		});
	},

	updateStores: function(record) {
		record.texts().loadData(record.get('texts'));
		record.labels().loadData(record.get('labels'));
		record.logos().loadData(record.get('logos'));
		record.landmarks().loadData(record.get('landmarks'));
		record.faces().loadData(record.get('faces'));

		this.getViewModel().set('selectedImage', null);
		this.getViewModel().set('selectedImage', record);
	}

});
