package spinyq.spiny_textiles.util;

public class Color4f extends Color3f {

	protected float a;

	public Color4f() {
		super();
	}

	public Color4f(float r, float g, float b, float a) {
		super(r, g, b);
		this.a = a;
	}
	
	public Color4f(Color3f rgb, float a) {
		super(rgb);
		this.a = a;
	}

	public float getA() {
		return a;
	}

	public void setA(float a) {
		this.a = a;
	}
	
	public int toIntARGB() {
		int n = 0;
		n += ((int) (a * 255)) << 24;
		n += ((int) (r * 255)) << 16;
		n += ((int) (g * 255)) << 8;
		n += ((int) (b * 255));
		return n;
	}
	
	public Color4f fromIntARGB(int argb) {
		a = (float) (argb >> 24 & 255) / 255.0F;
		r = (float) (argb >> 16 & 255) / 255.0F;
        g = (float) (argb >> 8 & 255) / 255.0F;
        b = (float) (argb & 255) / 255.0F;
        return this;
	}
	
}
