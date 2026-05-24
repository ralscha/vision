import { CommonModule } from '@angular/common';
import { Component, ElementRef, computed, effect, inject, signal, viewChild } from '@angular/core';
import * as maplibregl from 'maplibre-gl';
import { firstValueFrom } from 'rxjs';

import { VisionApiService } from './vision-api.service';
import { Face, FaceLandmark, LngLat, SafeSearch, Vertex, VisionImage } from './vision.models';

type TabId = 'labels' | 'web' | 'faces' | 'landmarks' | 'logos' | 'text' | 'safe-search';
type UploadMode = 'backend' | 'presigned';

@Component({
  selector: 'app-root',
  imports: [CommonModule],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  private readonly visionApi = inject(VisionApiService);
  private readonly defaultCenter: LngLat = { lat: 36.53387, lng: -22.71878 };
  private readonly defaultZoom = 2;
  private map: maplibregl.Map | null = null;
  private markers: maplibregl.Marker[] = [];

  protected readonly tabs: { id: TabId; label: string }[] = [
    { id: 'labels', label: 'Labels' },
    { id: 'web', label: 'Web' },
    { id: 'faces', label: 'Faces' },
    { id: 'landmarks', label: 'Landmarks' },
    { id: 'logos', label: 'Logos' },
    { id: 'text', label: 'Text' },
    { id: 'safe-search', label: 'Safe Search' },
  ];

  protected readonly images = signal<VisionImage[]>([]);
  protected readonly selectedImageId = signal<number | null>(null);
  protected readonly activeTab = signal<TabId>('labels');
  protected readonly isBusy = signal(false);
  protected readonly errorMessage = signal('');
  protected readonly imageSize = signal({ width: 1600, height: 900 });
  protected readonly selectedLocations = signal<LngLat[]>([]);
  protected readonly selectedFace = signal<Face | null>(null);
  protected readonly selectedFaceLandmark = signal<FaceLandmark | null>(null);
  protected readonly mapContainer = viewChild<ElementRef<HTMLDivElement>>('mapContainer');
  private readonly selectedPolygon = signal<Vertex[] | null>(null);

  protected readonly selectedImage = computed(
    () => this.images().find((image) => image.id === this.selectedImageId()) ?? null,
  );

  protected readonly displayImageUrl = computed(() => {
    const image = this.selectedImage();
    return image ? this.visionApi.imageUrl(image.id) : null;
  });

  protected readonly viewBox = computed(
    () => `0 0 ${this.imageSize().width} ${this.imageSize().height}`,
  );

  protected readonly selectedPolygonPoints = computed(() => {
    const polygon = this.selectedPolygon();
    if (!polygon?.length) {
      return '';
    }

    return polygon
      .filter((vertex) => vertex.x != null && vertex.y != null)
      .map((vertex) => `${vertex.x},${vertex.y}`)
      .join(' ');
  });

  protected readonly selectedPoint = computed(() => {
    const landmark = this.selectedFaceLandmark();
    return landmark ? { x: landmark.x, y: landmark.y } : null;
  });

  constructor() {
    effect(() => {
      const selectedImage = this.selectedImage();
      const imageUrl = this.displayImageUrl();

      this.selectedPolygon.set(null);
      this.selectedLocations.set([]);
      this.selectedFace.set(null);
      this.selectedFaceLandmark.set(null);

      if (!selectedImage || !imageUrl) {
        return;
      }

      void this.loadImageDimensions(imageUrl);
    });

    effect(() => {
      const tabId = this.activeTab();
      const mapContainer = this.mapContainer();

      if (tabId !== 'landmarks' || !mapContainer) {
        // Container just left the DOM — destroy the map so it is recreated fresh next visit
        if (this.map) {
          this.map.remove();
          this.map = null;
          this.markers = [];
        }
        return;
      }

      this.ensureMap(mapContainer.nativeElement);
    });

    effect(() => {
      if (this.activeTab() !== 'landmarks' || !this.map) {
        return;
      }

      this.syncMapToLocations(this.selectedLocations());
    });

    void this.loadImages();
  }

  protected async onFileSelected(event: Event, mode: UploadMode): Promise<void> {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];

    if (!file) {
      return;
    }

    this.errorMessage.set('');
    this.isBusy.set(true);

    try {
      const created = await this.uploadImage(file, mode);
      this.images.update((images) => [...images, created]);
      this.selectImage(created.id);
    } catch {
      this.errorMessage.set(this.uploadErrorMessage(mode));
    } finally {
      this.isBusy.set(false);
      input.value = '';
    }
  }

  protected selectImage(id: number): void {
    this.selectedImageId.set(id);
  }

  protected setActiveTab(tabId: TabId): void {
    this.activeTab.set(tabId);
    this.selectedPolygon.set(null);
    this.selectedLocations.set([]);
    this.selectedFace.set(null);
    this.selectedFaceLandmark.set(null);
  }

  protected selectFace(face: Face): void {
    this.activeTab.set('faces');
    this.selectedFace.set(face);
    this.selectedFaceLandmark.set(null);
    this.selectedLocations.set([]);
    this.selectedPolygon.set(face.boundingPoly?.length ? face.boundingPoly : face.fdBoundingPoly);
  }

  protected selectFaceLandmark(landmark: FaceLandmark): void {
    this.selectedFaceLandmark.set(landmark);
  }

  protected highlightPolygon(
    vertices: Vertex[] | null | undefined,
    locations: LngLat[] = [],
  ): void {
    this.selectedPolygon.set(vertices?.length ? vertices : null);
    this.selectedLocations.set(locations);
    this.selectedFaceLandmark.set(null);
  }

  protected async deleteSelectedImage(): Promise<void> {
    const image = this.selectedImage();
    if (!image || this.isBusy()) {
      return;
    }

    const confirmed = window.confirm(`Delete ${image.name}?`);
    if (!confirmed) {
      return;
    }

    this.isBusy.set(true);
    this.errorMessage.set('');

    try {
      await firstValueFrom(this.visionApi.deleteImage(image.id));
      const remaining = this.images().filter((entry) => entry.id !== image.id);
      this.images.set(remaining);
      this.selectedImageId.set(remaining[0]?.id ?? null);
    } catch {
      this.errorMessage.set('Delete failed. Verify the Spring backend is running on port 8080.');
    } finally {
      this.isBusy.set(false);
    }
  }

  protected thumbnailUrl(id: number): string {
    return this.visionApi.thumbnailUrl(id);
  }

  protected openStreetMapUrl(location: LngLat): string {
    return `https://www.openstreetmap.org/?mlat=${location.lat}&mlon=${location.lng}#map=11/${location.lat}/${location.lng}`;
  }

  protected asPercent(value: number | null | undefined): string {
    if (value == null || Number.isNaN(value)) {
      return '0%';
    }

    return `${Math.round(value * 100)}%`;
  }

  protected formatBytes(size: number | null | undefined): string {
    if (!size) {
      return '0 B';
    }

    const units = ['B', 'KB', 'MB', 'GB'];
    let value = size;
    let unitIndex = 0;

    while (value >= 1024 && unitIndex < units.length - 1) {
      value /= 1024;
      unitIndex += 1;
    }

    return `${value.toFixed(value >= 10 || unitIndex === 0 ? 0 : 1)} ${units[unitIndex]}`;
  }

  protected safeSearchEntries(): { label: string; value: string; rating: number }[] {
    const safeSearch: SafeSearch | null | undefined = this.selectedImage()?.safeSearch;
    if (!safeSearch) {
      return [];
    }

    return [
      { label: 'Adult', value: safeSearch.adult, rating: safeSearch.adultRating },
      { label: 'Spoof', value: safeSearch.spoof, rating: safeSearch.spoofRating },
      { label: 'Medical', value: safeSearch.medical, rating: safeSearch.medicalRating },
      { label: 'Violence', value: safeSearch.violence, rating: safeSearch.violenceRating },
    ];
  }

  private async loadImages(): Promise<void> {
    this.errorMessage.set('');

    try {
      const images = await firstValueFrom(this.visionApi.listImages());
      this.images.set(images);
      this.selectedImageId.set(images[0]?.id ?? null);
    } catch {
      this.errorMessage.set(
        'Unable to load images. Start the Spring backend on port 8080 and refresh.',
      );
    }
  }

  private async loadImageDimensions(url: string): Promise<void> {
    try {
      const image = await this.preloadImage(url);
      this.imageSize.set({ width: image.naturalWidth || 1600, height: image.naturalHeight || 900 });
    } catch {
      this.imageSize.set({ width: 1600, height: 900 });
    }
  }

  private ensureMap(container: HTMLDivElement): void {
    if (!this.map) {
      this.createMap(container);
    }

    const map = this.map;
    if (!map) {
      return;
    }

    map.resize();
    this.syncMapToLocations(this.selectedLocations());
  }

  private createMap(container: HTMLDivElement): void {
    this.map = new maplibregl.Map({
      container,
      style: {
        version: 8,
        sources: {
          osm: {
            type: 'raster',
            tiles: ['https://tile.openstreetmap.org/{z}/{x}/{y}.png'],
            tileSize: 256,
            attribution: '&copy; OpenStreetMap contributors',
          },
        },
        layers: [
          {
            id: 'osm',
            type: 'raster',
            source: 'osm',
          },
        ],
      },
      center: [this.defaultCenter.lng, this.defaultCenter.lat],
      zoom: this.defaultZoom,
    });

    this.map.addControl(new maplibregl.NavigationControl(), 'top-right');
    this.map.once('load', () => {
      this.syncMapToLocations(this.selectedLocations());
    });
  }

  private syncMapToLocations(locations: LngLat[]): void {
    if (!this.map) {
      return;
    }

    this.map.stop();
    this.clearMarkers();

    if (!locations.length) {
      this.map.jumpTo({
        center: [this.defaultCenter.lng, this.defaultCenter.lat],
        zoom: this.defaultZoom,
      });
      return;
    }

    for (const location of locations) {
      const marker = new maplibregl.Marker({ color: '#8d3d2d' })
        .setLngLat([location.lng, location.lat])
        .addTo(this.map);
      this.markers.push(marker);
    }

    if (locations.length === 1) {
      const [location] = locations;
      this.map.jumpTo({
        center: [location.lng, location.lat],
        zoom: 11,
      });
      return;
    }

    const bounds = locations.reduce(
      (currentBounds, location) =>
        currentBounds.extend([location.lng, location.lat] as [number, number]),
      new maplibregl.LngLatBounds(
        [locations[0].lng, locations[0].lat],
        [locations[0].lng, locations[0].lat],
      ),
    );

    this.map.fitBounds(bounds, { padding: 48, duration: 0, maxZoom: 11 });
  }

  private clearMarkers(): void {
    for (const marker of this.markers) {
      marker.remove();
    }
    this.markers = [];
  }

  private preloadImage(url: string): Promise<HTMLImageElement> {
    return new Promise((resolve, reject) => {
      const image = new Image();
      image.onload = () => resolve(image);
      image.onerror = () => reject(new Error('Image failed to load'));
      image.src = url;
    });
  }

  private readFileAsDataUrl(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onerror = () => reject(new Error('File failed to load'));
      reader.onload = () => resolve(String(reader.result));
      reader.readAsDataURL(file);
    });
  }

  private async uploadImage(file: File, mode: UploadMode): Promise<VisionImage> {
    if (mode === 'presigned') {
      return firstValueFrom(this.visionApi.uploadImageViaPresignedUrl(file));
    }

    const dataUrl = await this.readFileAsDataUrl(file);
    return firstValueFrom(this.visionApi.uploadImage(file, dataUrl));
  }

  private uploadErrorMessage(mode: UploadMode): string {
    if (mode === 'presigned') {
      return 'Signed upload failed. Verify the backend is running, app.storage-bucket is configured, and the bucket allows browser PUT requests.';
    }

    return 'Image upload failed. Verify the Spring backend is running on port 8080.';
  }
}
