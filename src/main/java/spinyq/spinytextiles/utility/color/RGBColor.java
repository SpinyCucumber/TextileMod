package spinyq.spinytextiles.utility.color;

import java.util.Optional;
import java.util.Random;

import net.minecraft.item.DyeColor;

/**
 * Utility class to help with converting between different color formats and using colors.
 * @author SpinyQ
 *
 */
public class RGBColor {

	public float r, g, b;

	public RGBColor() { }
	
	public RGBColor(float r, float g, float b) {
		this.r = r;
		this.g = g;
		this.b = b;
	}
	
	public void setAll(RGBColor other) {
		this.r = other.r;
		this.g = other.g;
		this.b = other.b;
	}
	
	public RGBColor copy() {
		return new RGBColor(r, g, b);
	}
	
	public RGBColor interp(RGBColor other, float s) {
		return new RGBColor(((other.r - r) * s) + r, ((other.g - g) * s) + g, ((other.b - b) * s) + b);
	}
	
	public RGBColor scale(float factor) {
		r *= factor;
		g *= factor;
		b *= factor;
		return this;
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
	 * Sets this color using an integer
	 * @param hex An integer in hex rgb format, i.e. 0xRRGGBB
	 * @return
	 */
	public RGBColor fromInt(int hex)
	{
		r = (float) (hex >> 16 & 255) / 255.0F;
        g = (float) (hex >> 8 & 255) / 255.0F;
        b = (float) (hex & 255) / 255.0F;
        return this;
	}
	
	/**
	 * Constructs a color using a Minecraft dye color.
	 * @param dye
	 * @return
	 */
	public RGBColor fromDye(DyeColor dye) {
		return this.fromInt(dye.getTextColor());
	}
	
	/**
	 * Just a convenience method that uses Integer.decode to convert an integer string (e.g. a hex string) into a color.
	 * @param hex
	 * @return
	 */
	public RGBColor fromIntString(String hex)
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
	public RGBColor fromIntRGB(int r, int g, int b)
	{
		this.r = r / 255.0f;
		this.g = g/ 255.0f;
		this.b = b / 255.0f;
		return this;
	}
	
	/**
	 * Converts this color (in RGB space) into HSV space and stores is in an HSV object.
	 * @return
	 */
	public HSVColor toHSV(HSVColor hsv)
	{
		hsv.fromRGB(this);
		return hsv;
	}

	/**
	 * Sets this color HSV space to RGB space with alpha = 1.0
	 * @param hue The color hue, from 0.0 to 360.0
	 * @param sat Saturation
	 * @param val Value/Lightness
	 * @return
	 */
	public RGBColor fromHSV(HSVColor hsv)
	{
		hsv.toRGB(this);
		return this;
	}
	
	public RYBKColor toRYB(RYBKColor ryb, Optional<RGBColor> base) {
		ryb.fromRGB(this, base);
		return ryb;
	}
	
	public RGBColor fromRYB(RYBKColor ryb, Optional<RGBColor> base) {
		ryb.toRGB(this, base);
		return this;
	}
	
	public double dist(RGBColor other) {
		return Math.sqrt(Math.pow(r - other.r, 2.0f) + Math.pow(g - other.g, 2.0f) + Math.pow(b - other.b, 2.0f));
	}
	
	public double distSquared(RGBColor other) {
		return Math.pow(other.r - r, 2.0) + Math.pow(other.g - g, 2.0) + Math.pow(other.b - b, 2.0);
	}

	@Override
	public String toString() {
		return "Color [r=" + r + ", g=" + g + ", b=" + b + "]";
	}

	/**
	 * Returns a new color with RGB values uniformly distributed between 0 and 1.
	 * @param random
	 * @return
	 */
	public static RGBColor random(Random random) {
		return new RGBColor(random.nextFloat(), random.nextFloat(), random.nextFloat());
	}
	
}
