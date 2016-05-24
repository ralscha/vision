package ch.rasc.vision.controller;

public enum Likelihood {

	UNKNOWN(0.0f), VERY_UNLIKELY(0.2f), UNLIKELY(0.4f), POSSIBLE(0.6f), LIKELY(0.8f),
	VERY_LIKELY(1.0f);

	private final float rating;

	private Likelihood(float rating) {
		this.rating = rating;
	}

	public float getRating() {
		return this.rating;
	}

	public static Likelihood of(String value) {
		switch (value) {
		case "UNKNOWN":
			return UNKNOWN;
		case "VERY_UNLIKELY":
			return VERY_UNLIKELY;
		case "UNLIKELY":
			return UNLIKELY;
		case "POSSIBLE":
			return POSSIBLE;
		case "LIKELY":
			return LIKELY;
		case "VERY_LIKELY":
			return VERY_LIKELY;
		default:
			return UNKNOWN;
		}
	}

}
