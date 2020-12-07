package spinyq.spinytextiles.utility.color;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;

import net.minecraft.item.DyeColor;
import net.minecraft.nbt.IntNBT;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Using Idealized RYB Model defined by following paper with an additional black channel to allow for pure blacks
 * https://bahamas10.github.io/ryb/assets/ryb.pdf
 * by Dr. Nathan Gossett and Dr. Baoquan Chen of the University of Minnesota at Twin Cities
 * @author Elijah Hilty
 *
 */
public class RYBKColor implements INBTSerializable<IntNBT> {

	public static enum Axis {
		
		RED(new RYBKColor(1.0f, 0.0f, 0.0f, 0.0f)),
		YELLOW(new RYBKColor(0.0f, 1.0f, 0.0f, 0.0f)),
		BLUE(new RYBKColor(0.0f, 0.0f, 1.0f, 0.0f)),
		BLACK(new RYBKColor(0.0f, 0.0f, 0.0f, 1.0f));
		
		public final RYBKColor direction;
		
		private Axis(RYBKColor direction) {
			this.direction = direction;
		}
		
	}
	
	private static final RGBColor[] RGB_POINTS = {
			new RGBColor(1.0f, 1.0f, 1.0f), // WHITE
			new RGBColor(0.163f, 0.373f, 0.95f), // BLUE
			new RGBColor(1.0f, 1.0f, 0.0f), // YELLOW
			new RGBColor(0.0f, 0.95f, 0.2f), // GREEN
			new RGBColor(1.0f, 0.0f, 0.0f), // RED
			new RGBColor(0.75f, 0.0f, 0.75f), // PURPLE
			new RGBColor(1.0f, 0.5f, 0.0f), // ORANGE
			new RGBColor(0.2f, 0.094f, 0.0f), // BROWN
	};
	
	private static final RGBColor BLACK = new RGBColor(0.1f, 0.1f, 0.1f);
	
	// Cache the RYB colors of different dyes to speed things up
	private static final Map<DyeColor, RYBKColor> DYE_MAP = new EnumMap<>(new ImmutableMap.Builder<DyeColor, RYBKColor>()
			.put(DyeColor.BLACK, new RYBKColor(0.0f, 0.0f, 0.0f, 1.0f))
			.put(DyeColor.BLUE, new RYBKColor(0.0f, 0.0f, 1.0f, 0.0f))
			.put(DyeColor.BROWN, new RYBKColor(1.0f, 1.0f, 1.0f, 0.0f))
			.put(DyeColor.CYAN, new RYBKColor(0.0f, 0.0f, 0.25f, 0.0f))
			.put(DyeColor.GRAY, new RYBKColor(0.0f, 0.0f, 0.0f, 0.5f))
			.put(DyeColor.GREEN, new RYBKColor(0.0f, 1.0f, 1.0f, 0.0f))
			.put(DyeColor.LIGHT_BLUE, new RYBKColor(0.0f, 0.0f, 0.5f, 0.0f))
			.put(DyeColor.LIGHT_GRAY, new RYBKColor(0.0f, 0.0f, 0.0f, 0.25f))
			.put(DyeColor.LIME, new RYBKColor(0.0f, 0.5f, 0.5f, 0.0f))
			.put(DyeColor.MAGENTA, new RYBKColor(0.5f, 0.0f, 0.5f, 0.0f))
			.put(DyeColor.ORANGE, new RYBKColor(1.0f, 1.0f, 0.0f, 0.0f))
			.put(DyeColor.PINK, new RYBKColor(0.5f, 0.0f, 0.0f, 0.0f))
			.put(DyeColor.PURPLE, new RYBKColor(1.0f, 0.0f, 1.0f, 0.0f))
			.put(DyeColor.RED, new RYBKColor(1.0f, 0.0f, 0.0f, 0.0f))
			.put(DyeColor.WHITE, new RYBKColor(0.0f, 0.0f, 0.0f, 0.0f))
			.put(DyeColor.YELLOW, new RYBKColor(0.0f, 1.0f, 0.0f, 0.0f)).build());
	
	// Used to "bias" the interpolation factor towards the corners of the cube
	private static final Function<Float, Float> BIAS_FUNCTION = (f) -> f*f*(3-2*f);
	
	private static RGBColor getRGBPoint(int r, int y, int b) {
		// Red is primary axis, yellow is secondary, blue is tertiary
		// So (0, 0, 0) -> 0, (0, 0, 1) -> 1, (0, 1, 0) -> 2, (0, 1, 1) -> 3, ...
		// (1, 1, 0) would represent a mixture of red and yellow and would be orange
		int n = 1, index = 0;
		index += b * n;
		n *= 2;
		index += y * n;
		n *= 2;
		index += r * n;
		return RGB_POINTS[index];
	}
	
	// "How much" red, yellow, blue, and black this color has. Between 0 and 1 inclusive
	private float r, y, b, k;
	
	public RYBKColor() {
		this.r = 0.0f;
		this.y = 0.0f;
		this.b = 0.0f;
		this.k = 0.0f;
	}
	
	public RYBKColor(float r, float y, float b, float k) {
		this.r = r;
		this.y = y;
		this.b = b;
		this.k = k;
	}
	
	public RYBKColor(float s) {
		this(s, s, s, s);
	}
	
	public RYBKColor setAll(RYBKColor other) {
		this.r = other.r;
		this.y = other.y;
		this.b = other.b;
		this.k = other.k;
		return this;
	}
	
	public RYBKColor copy() {
		return new RYBKColor(r, y, b, k);
	}

	// Modifies this color
	public RYBKColor add(RYBKColor other) {
		this.r += other.r;
		this.y += other.y;
		this.b += other.b;
		this.k += other.k;
		return this;
	}
	
	// Modifies this color
	public RYBKColor clamp() {
		this.r = Math.min(Math.max(r, 0.0f), 1.0f);
		this.y = Math.min(Math.max(y, 0.0f), 1.0f);
		this.b = Math.min(Math.max(b, 0.0f), 1.0f);
		this.k = Math.min(Math.max(k, 0.0f), 1.0f);
		return this;
	}
	
	// Does not modify
	public RYBKColor plus(RYBKColor other) {
		return new RYBKColor(r + other.r, y + other.y, b + other.b, k + other.k);
	}
	
	// Does not modify
	public RYBKColor minus(RYBKColor other) {
		return new RYBKColor(r - other.r, y - other.y, b - other.b, k - other.k);
	}
	
	// Does not modify
	public RYBKColor scaledBy(double factor) {
		return new RYBKColor((float) (r * factor), (float) (y * factor), (float) (b * factor), (float) (k * factor));
	}
	
	// Does not modify
	public RYBKColor interp(RYBKColor other, float factor) {
		return new RYBKColor(((other.r - r) * factor) + r, ((other.y - y) * factor) + y, ((other.b - b) * factor) + b, ((other.k - k) * factor) + k);
	}
	
	public double dist(RYBKColor other) {
		return Math.sqrt(Math.pow(r - other.r, 2.0f) + Math.pow(y - other.y, 2.0f) + Math.pow(b - other.b, 2.0f) + Math.pow(k - other.k, 2.0f));
	}
	
	// Dot product
	public float project(RYBKColor other) {
		return (r * other.r) + (y * other.y) + (b * other.b) + (k * other.k);
	}
	
	/**
	 * Converts color into an integer of format 0xRRGGBBKK
	 * @return
	 */
	public int toInt() {
		int n = 0;
		n += ((int) (r * 255)) << 24;
		n += ((int) (y * 255)) << 16;
		n += ((int) (b * 255)) << 8;
		n += ((int) (k * 255));
		return n;
	}

	/**
	 * Sets this color using an integer of format 0xRRGGBBKK
	 */
	public RYBKColor fromInt(int n)
	{
		r = (float) (n >> 24 & 255) / 255.0F;
        y = (float) (n >> 16 & 255) / 255.0F;
        b = (float) (n >> 8 & 255) / 255.0F;
        k = (float) (n & 255) / 255.0F;
        return this;
	} 
	
	@Override
	public String toString() {
		return "RYBKColor [r=" + r + ", y=" + y + ", b=" + b + ", k=" + k + "]";
	}

	public boolean hasValue() {
		return r > 0.0f || y > 0.0f || b > 0.0f || k > 0.0f;
	}

	public RGBColor toRGB(RGBColor color, RGBColor base) {
		// Apply bias function to color values
		float rb = BIAS_FUNCTION.apply(r),
				yb = BIAS_FUNCTION.apply(y),
				bb = BIAS_FUNCTION.apply(b);
		// Trilinear interpolation
		RGBColor c000 = (base == null) ? getRGBPoint(0,0,0) : base,
				c001 = getRGBPoint(0,0,1),
				c010 = getRGBPoint(0,1,0),
				c011 = getRGBPoint(0,1,1),
				c100 = getRGBPoint(1,0,0),
				c101 = getRGBPoint(1,0,1),
				c110 = getRGBPoint(1,1,0),
				c111 = getRGBPoint(1,1,1);
		// Interpolate along blue axis
		RGBColor c00 = c000.interp(c001, bb),
				c01 = c010.interp(c011, bb),
				c10 = c100.interp(c101, bb),
				c11 = c110.interp(c111, bb);
		// Inteprolate along yellow axis
		RGBColor c0 = c00.interp(c01, yb),
				c1 = c10.interp(c11, yb);
		// Finally, interpolate along red axis
		// Also apply black
		RGBColor c = c0.interp(c1, rb).interp(BLACK, k);
		color.setAll(c);
		return color;
	}

	@Deprecated
	public RYBKColor fromRGB(RGBColor rgb, RGBColor base) {
		// Our initial guess, arbitrary
		RYBKColor guess = new RYBKColor(0.5f, 0.5f, 0.5f, 0.5f);
		// The function to minimize
		Function<RYBKColor, Double> errorFunction = (color) -> { return color.toRGB(new RGBColor(), base).distSquared(rgb); };
		double epsilon = 0.00001, margin = 0.00001, error;
		int maxIters = 400, iters = 0;
		// Keep adjusting guess until error is small enough
		while (true) {
			iters++;
			// Calculate error, stop if close enough
			error = errorFunction.apply(guess);
			if (error < margin) break;
			// To adjust guess, construct gradient by approximating partial derivatives along each axis
			RYBKColor gradient = new RYBKColor();
			for (Axis axis : Axis.values()) {
				// Create sample point
				RYBKColor sample = guess.plus(axis.direction.scaledBy(epsilon));
				// Calculate error change in direction and add it to gradient
				double errorChange = (errorFunction.apply(sample) - error) / epsilon;
				gradient.add(axis.direction.scaledBy(errorChange));
			}
			// DEBUG
			// System.out.println(String.format("Guess: %s Error: %f Gradient: %s", guess, error, gradient));
			// Move guess by opposite of gradient
			// Break if maximum iterations reached
			guess.add(gradient.scaledBy(-0.2));
			if (iters == maxIters) break;
		}
		// Set ourselves to be the adjusted guess
		guess.clamp();
		this.setAll(guess);
		return this;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(b);
		result = prime * result + Float.floatToIntBits(k);
		result = prime * result + Float.floatToIntBits(r);
		result = prime * result + Float.floatToIntBits(y);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RYBKColor other = (RYBKColor) obj;
		if (Float.floatToIntBits(b) != Float.floatToIntBits(other.b))
			return false;
		if (Float.floatToIntBits(k) != Float.floatToIntBits(other.k))
			return false;
		if (Float.floatToIntBits(r) != Float.floatToIntBits(other.r))
			return false;
		if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y))
			return false;
		return true;
	}

	public RYBKColor fromDye(DyeColor dye) {
		return DYE_MAP.get(dye);
	}

	public HSVColor toHSV(HSVColor hsv, RGBColor base) {
		// This has the potential to be finnicky
		return this.toRGB(new RGBColor(), base).toHSV(hsv);
	}
	
	@Deprecated
	public RYBKColor fromHSV(HSVColor hsv, RGBColor base) {
		return this.fromRGB(new RGBColor().fromHSV(hsv), base);
	}

	@Override
	public IntNBT serializeNBT() {
		return IntNBT.valueOf(this.toInt());
	}

	@Override
	public void deserializeNBT(IntNBT nbt) {
		fromInt(nbt.getInt());
	}
	
}
