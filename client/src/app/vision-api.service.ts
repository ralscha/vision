import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';

import { VisionImage } from './vision.models';

interface ApiResult<T> {
  records: T[];
}

@Injectable({ providedIn: 'root' })
export class VisionApiService {
  private readonly http = inject(HttpClient);
  private readonly imagesUrl = '/api/images';

  listImages(): Observable<VisionImage[]> {
    return this.http.get<VisionImage[]>(this.imagesUrl);
  }

  uploadImage(file: File, dataUrl: string): Observable<VisionImage> {
    return this.http
      .post<ApiResult<VisionImage>>(this.imagesUrl, {
        name: file.name,
        type: file.type,
        size: file.size,
        data: dataUrl,
      })
      .pipe(
        map((response) => {
          const image = response.records[0];

          if (!image) {
            throw new Error('Upload response did not include an image record.');
          }

          return image;
        }),
      );
  }

  deleteImage(id: number): Observable<boolean> {
    return this.http.delete<boolean>(`${this.imagesUrl}/${id}`);
  }

  imageUrl(id: number): string {
    return `/image/${id}`;
  }

  thumbnailUrl(id: number): string {
    return `/thumbnail/${id}`;
  }
}
