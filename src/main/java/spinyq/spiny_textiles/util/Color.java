package spinyq.spiny_textiles.util;

/**
 * Utility class to help with converting between different color formats and using colors.
 * @author SpinyQ
 *
 */
public class Color {

	public static class HSV
	{
		public float hue, sat, val;

		public HSV(float hue, float sat, float val) {
			super();
			this.hue = hue;
			this.sat = sat;
			this.val = val;
		}

		public HSV() {
			super();
		}
		
	}
	
	private float r, g, b;

	public Color() { }
	
	public Color(float r, float g, float b) {
		super();
		this.r = r;
		this.g = g;
		this.b = b;
	}
	
	public Color(Color other) {
		this(other.r, other.g, other.b);
	}

	public float getR() {
		return r;
	}

	public void setR(float r) {
		this.r = r;
	}

	public float getG() {
		return g;
	}

	public void setG(float g) {
		this.g = g;
	}

	public float getB() {
		return b;
	}

	public void setB(float b) {
		this.b = b;
	}
	
	/**
	 * Converts color into an integer of format 0xRRGGBB
	 * @return
	 */
	public int toInt() {
		int n = 0;
		n += ((int) (r * 255)) << 16;
		n += ((int) (g * 255)) << 8;
		n += ((int) (b * 255));
		return n;
	}
	
	/**
	 * Converts this color (in RGB space) into HSV space and stores is in an HSV object.
	 * @return
	 */
	public void toHSV(HSV hsv)
	{
		float max = Math.max(r, Math.max(g, b));
		float min = Math.min(r, Math.min(g, b));
		
		// Calculate hue
		float hue = 0.0f;
		if (max == min) hue = 0.0f; // No hue
		else if (max == r) hue = 60.0f * (g - b) / (max - min);
		else if (max == g) hue = 60.0f * (2.0f + (b - r) / (max - min));
		else if (max == b) hue = 60.0f * (4.0f + (r - g) / (max - min));
		if (hue < 0) hue += 360.0f;
		hsv.hue = hue;
		
		// Calculate saturation. Value is just max
		hsv.sat = (max == 0.0f) ? 0.0f : (max - min) / max;
		hsv.val = max;

	}
	
	/**
	 * Constructs a new color from an integer. Sets alpha to 1.0
	 * @param hex An integer in hex rgb format, i.e. 0xRRGGBB
	 * @return
	 */
	public static Color fromInt(int hex)
	{
		float r = (float) (hex >> 16 & 255) / 255.0F;
        float g = (float) (hex >> 8 & 255) / 255.0F;
        float b = (float) (hex & 255) / 255.0F;
        return new Color(r, g, b);
	}
	
	/**
	 * Just a convenience method that uses Integer.decode to convert an integer string (e.g. a hex string) into a color.
	 * @param hex
	 * @return
	 */
	public static Color fromIntString(String hex)
	{
		return fromInt(Integer.decode(hex));
	}
	
	/**
	 * Constructs a new color with alpha = 1.0 given integers r, g, and b are between 0 and 255.
	 * @param r
	 * @param g
	 * @param b
	 * @return
	 */
	public static Color fromIntRGB(int r, int g, int b)
	{
		return new Color(r / 255.0f, g / 255.0f, b / 255.0f);
	}
	
	/**
	 * Sets this color HSV space to RGB space with alpha = 1.0
	 * @param hue The color hue, from 0.0 to 360.0
	 * @param sat Saturation
	 * @param val Value/Lightness
	 * @return
	 */
	public void fromHSV(HSV hsv)
	{
		float chroma = hsv.val * hsv.sat;
		float hp = hsv.hue / 60.0f;
		float x = chroma * (1.0f - Math.abs(hp % 2.0f - 1.0f));
		
		// Default value
		// Find point on "color cube"
		float[] rgb = new float[] {0.0f, 0.0f, 0.0f};
		if (hp < 1.0f) {
			rgb = new float[] {chroma, x, 0.0f};
		} else if (hp < 2.0f) {
			rgb = new float[] {x, chroma, 0.0f};
		} else if (hp < 3.0f) {
			rgb = new float[] {0.0f, chroma, x};
		} else if (hp < 4.0f) {
			rgb = new float[] {0.0f, x, chroma};
		} else if (hp < 5.0f) {
			rgb = new float[] {x, 0.0f, chroma};
		} else if (hp < 6.0f) {
			rgb = new float[] {chroma, 0.0f, x};
		}
		
		// Set all our junk
		float m = hsv.val - chroma;
		r = rgb[0] + m;
		g = rgb[1] + m;
		b = rgb[2] + m;
		
	}
	
	public double distanceTo(Color other) {
		return Math.sqrt(Math.pow(r - other.r, 2.0f) + Math.pow(g - other.g, 2.0f) + Math.pow(b - other.b, 2.0f));
	}

	@Override
	public String toString() {
		return "Color [r=" + r + ", g=" + g + ", b=" + b + "]";
	}
	
}
