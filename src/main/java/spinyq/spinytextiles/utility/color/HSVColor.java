package spinyq.spinytextiles.utility.color;

public class HSVColor {
	
	public float hue, sat, val;

	public HSVColor(float hue, float sat, float val) {
		this.hue = hue;
		this.sat = sat;
		this.val = val;
	}
	
	public HSVColor() {
		this.hue = 0;
		this.sat = 0;
		this.val = 0;
	}

	public HSVColor fromRGB(RGBColor rgb) {
		float max = Math.max(rgb.r, Math.max(rgb.g, rgb.b));
		float min = Math.min(rgb.r, Math.min(rgb.g, rgb.b));
		
		// Calculate hue
		if (max == min) hue = 0.0f; // No hue
		else if (max == rgb.r) hue = 60.0f * (rgb.g - rgb.b) / (max - min);
		else if (max == rgb.g) hue = 60.0f * (2.0f + (rgb.b - rgb.r) / (max - min));
		else if (max == rgb.b) hue = 60.0f * (4.0f + (rgb.r - rgb.g) / (max - min));
		if (hue < 0) hue += 360.0f;
		
		// Calculate saturation. Value is just max
		sat = (max == 0.0f) ? 0.0f : (max - min) / max;
		val = max;
		return this;
	}
	
	public RGBColor toRGB(RGBColor rgb) {
		float chroma = val * sat;
		float hp = hue / 60.0f;
		float x = chroma * (1.0f - Math.abs(hp % 2.0f - 1.0f));
		
		// Default value
		// Find point on "color cube"
		float[] arr = new float[] {0.0f, 0.0f, 0.0f};
		if (hp < 1.0f) {
			arr = new float[] {chroma, x, 0.0f};
		} else if (hp < 2.0f) {
			arr = new float[] {x, chroma, 0.0f};
		} else if (hp < 3.0f) {
			arr = new float[] {0.0f, chroma, x};
		} else if (hp < 4.0f) {
			arr = new float[] {0.0f, x, chroma};
		} else if (hp < 5.0f) {
			arr = new float[] {x, 0.0f, chroma};
		} else if (hp < 6.0f) {
			arr = new float[] {chroma, 0.0f, x};
		}
		
		// Set all our junk
		float m = val - chroma;
		rgb.r = arr[0] + m;
		rgb.g = arr[1] + m;
		rgb.b = arr[2] + m;
		return rgb;
	}
	
}