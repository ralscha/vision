Ext.define('Vision.view.image.GMapPanel', {
	extend: 'Ext.panel.Panel',

	initComponent: function() {
		Ext.applyIf(this, {
			plain: true,
			gmapType: 'map',
			border: false
		});

		this.callParent();
	},

	onBoxReady: function() {
		var center = this.center;
		this.callParent(arguments);

		if (center) {
			if (center.geoCodeAddr) {
				this.lookupCode(center.geoCodeAddr, center.marker);
			}
			else {
				this.createMap(center);
			}
		}
	},

	createMap: function(center, marker) {
		var options = Ext.apply({}, this.mapOptions);

		options = Ext.applyIf(options, {
			zoom: 5,
			center: center,
			mapTypeId: google.maps.MapTypeId.ROADMAP
		});
		this.markers = [];
		this.gmap = new google.maps.Map(this.body.dom, options);
		if (marker) {
			this.addMarker(Ext.applyIf(marker, {
				position: center
			}));
		}

		this.fireEvent('mapready', this, this.gmap);
	},

	addMarker: function(marker) {
		marker = Ext.apply({
			map: this.gmap
		}, marker);

		if (!marker.position) {
			marker.position = new google.maps.LatLng(marker.lat, marker.lng);
		}
		var o = new google.maps.Marker(marker);
		Ext.Object.each(marker.listeners, function(name, fn) {
			o.addListener(name, fn);
		});
		this.markers.push(o);
		return o;
	},

	// Sets the map on all markers in the array.
	setMapOnAll: function(map) {
		if (this.markers) {
			for (var i = 0; i < this.markers.length; i++) {
				this.markers[i].setMap(map);
			}
		}
	},

	// Removes the markers from the map, but keeps them in the array.
	clearMarkers: function() {
		this.setMapOnAll(null);
	},

	// Shows any markers currently in the array.
	showMarkers: function() {
		this.setMapOnAll(this.gmap);
	},

	// Deletes all markers in the array by removing references to them.
	deleteMarkers: function() {
		this.clearMarkers();
		this.markers = [];
	},

	fitBounds: function(boundary) {
		var map = this.gmap;
		if (map) {
			map.fitBounds(boundary);
		}
	},

	setCenter: function(center) {
		var map = this.gmap;
		if (map) {
			map.setCenter(center);
		}
	},

	lookupCode: function(addr, marker) {
		this.geocoder = new google.maps.Geocoder();
		this.geocoder.geocode({
			address: addr
		}, Ext.Function.bind(this.onLookupComplete, this, [ marker ], true));
	},

	onLookupComplete: function(data, response, marker) {
		if (response != 'OK') {
			Ext.MessageBox.alert('Error', 'An error occured: "' + response + '"');
			return;
		}
		this.createMap(data[0].geometry.location, marker);
	},

	afterComponentLayout: function(w, h) {
		this.callParent(arguments);
		this.redraw();
	},

	redraw: function() {
		var map = this.gmap;
		if (map) {
			google.maps.event.trigger(map, 'resize');
		}
	}

});
