package spinyq.spinytextiles.utility.color;

public class RGBAColor extends RGBColor {

	public float a;

	public RGBAColor() {
		super();
	}

	public RGBAColor(float r, float g, float b, float a) {
		super(r, g, b);
		this.a = a;
	}
	
	public RGBAColor(RGBColor rgb, float a) {
		this(rgb.r, rgb.g, rgb.b, a);
	}
	
	public int toIntARGB() {
		int n = 0;
		n += ((int) (a * 255)) << 24;
		n += ((int) (r * 255)) << 16;
		n += ((int) (g * 255)) << 8;
		n += ((int) (b * 255));
		return n;
	}
	
	public RGBAColor fromIntARGB(int argb) {
		a = (float) (argb >> 24 & 255) / 255.0F;
		r = (float) (argb >> 16 & 255) / 255.0F;
        g = (float) (argb >> 8 & 255) / 255.0F;
        b = (float) (argb & 255) / 255.0F;
        return this;
	}
	
}
