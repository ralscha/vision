export interface Vertex {
  x: number | null;
  y: number | null;
}

export interface LngLat {
  lng: number;
  lat: number;
}

export interface Label {
  description: string;
  score: number;
}

export interface Logo {
  description: string;
  score: number;
  boundingPoly: Vertex[];
}

export interface Landmark {
  description: string;
  score: number;
  boundingPoly: Vertex[];
  locations: LngLat[];
}

export interface TextAnnotation {
  description: string;
  boundingPoly: Vertex[];
}

export interface FaceLandmark {
  type: string;
  x: number;
  y: number;
  z: number;
}

export interface Face {
  rollAngle: number;
  panAngle: number;
  tiltAngle: number;
  detectionConfidence: number;
  landmarkingConfidence: number;
  joy: string;
  sorrow: string;
  anger: string;
  surprise: string;
  underExposed: string;
  blurred: string;
  headwear: string;
  joyRating: number;
  sorrowRating: number;
  angerRating: number;
  surpriseRating: number;
  underExposedRating: number;
  blurredRating: number;
  headwearRating: number;
  boundingPoly: Vertex[];
  fdBoundingPoly: Vertex[];
  landmarks: FaceLandmark[];
}

export interface SafeSearch {
  adult: string;
  spoof: string;
  medical: string;
  violence: string;
  adultRating: number;
  spoofRating: number;
  medicalRating: number;
  violenceRating: number;
}

export interface WebEntity {
  entityId: string;
  score: number;
  description: string;
}

export interface WebUrl {
  url: string;
  score: number;
}

export interface WebDetection {
  webEntities: WebEntity[];
  fullMatchingImages: WebUrl[];
  partialMatchingImages: WebUrl[];
  pagesWithMatchingImages: WebUrl[];
}

export interface VisionImage {
  id: number;
  name: string;
  type: string;
  size: number;
  labels?: Label[];
  logos?: Logo[];
  landmarks?: Landmark[];
  texts?: TextAnnotation[];
  faces?: Face[];
  safeSearch: SafeSearch | null;
  web: WebDetection | null;
}
