package spinyq.spinytextiles.utility.color;

import java.util.function.Function;

/**
 * Using Idealized RYB Model defined by following paper
 * https://bahamas10.github.io/ryb/assets/ryb.pdf
 * by Dr. Nathan Gossett and Dr. Baoquan Chen of the University of Minnesota at Twin Cities
 * @author Elijah Hilty
 *
 */
public class RYBColor {

	public static enum Axis {
		
		RED(new RYBColor(1.0f, 0.0f, 0.0f)),
		YELLOW(new RYBColor(0.0f, 1.0f, 0.0f)),
		BLUE(new RYBColor(0.0f, 0.0f, 1.0f));
		
		public final RYBColor direction;
		
		private Axis(RYBColor direction) {
			this.direction = direction;
		}
		
	}
	
	private static final RGBColor[] RGB_POINTS = {
			new RGBColor(1.0f, 1.0f, 1.0f), // WHITE
			new RGBColor(0.163f, 0.373f, 0.6f), // BLUE
			new RGBColor(1.0f, 1.0f, 0.0f), // YELLOW
			new RGBColor(0.0f, 0.66f, 0.2f), // GREEN
			new RGBColor(1.0f, 0.0f, 0.0f), // RED
			new RGBColor(0.5f, 0.0f, 0.05f), // PURPLE
			new RGBColor(1.0f, 0.5f, 0.0f), // ORANGE
			new RGBColor(0.2f, 0.094f, 0.0f), // BROWN
	};
	
	// "How much" red, yellow, and blue this color has. Between 0 and 1 inclusive
	private float r, y, b;
	
	public RYBColor() {
		this.r = 0.0f;
		this.y = 0.0f;
		this.b = 0.0f;
	}
	
	public RYBColor(float r, float y, float b) {
		this.r = r;
		this.y = y;
		this.b = b;
	}
	
	public RYBColor setAll(RYBColor other) {
		this.r = other.r;
		this.y = other.y;
		this.b = other.b;
		return this;
	}
	
	// Modifies this color
	public RYBColor add(RYBColor other) {
		this.r += other.r;
		this.y += other.y;
		this.b += other.b;
		return this;
	}
	
	// Modifies this color
	public void clamp() {
		this.r = Math.min(Math.max(r, 0.0f), 1.0f);
		this.y = Math.min(Math.max(y, 0.0f), 1.0f);
		this.b = Math.min(Math.max(b, 0.0f), 1.0f);
	}
	
	// Does not modify
	public RYBColor plus(RYBColor other) {
		return new RYBColor(r + other.r, y + other.y, b + other.b);
	}
	
	// Does not modify
	public RYBColor scaledBy(double factor) {
		return new RYBColor((float) (r * factor), (float) (y * factor), (float) (b * factor));
	}
	
	// Does not modify
	public RYBColor interp(RYBColor other, float factor) {
		return new RYBColor(((other.r - r) * factor) + r, ((other.y - y) * y) + factor, ((other.b - b) * factor) + b);
	}
	
	public RGBColor toRGB(RGBColor color) {
		// Trilinear interpolation
		RGBColor c000 = getRGBPoint(0,0,0),
				c001 = getRGBPoint(0,0,1),
				c010 = getRGBPoint(0,1,0),
				c011 = getRGBPoint(0,1,1),
				c100 = getRGBPoint(1,0,0),
				c101 = getRGBPoint(1,0,1),
				c110 = getRGBPoint(1,1,0),
				c111 = getRGBPoint(1,1,1);
		// Interpolate along blue axis
		RGBColor c00 = c000.interp(c001, b),
				c01 = c010.interp(c011, b),
				c10 = c100.interp(c101, b),
				c11 = c110.interp(c111, b);
		// Inteprolate along yellow axis
		RGBColor c0 = c00.interp(c01, y),
				c1 = c10.interp(c11, y);
		// Finally, interpolate along red axis
		color.setAll(c0.interp(c1, r));
		return color;
	}

	@Override
	public String toString() {
		return "RYBColor [r=" + r + ", y=" + y + ", b=" + b + "]";
	}

	public RYBColor fromRGB(RGBColor rgb) {
		// Our initial guess
		RYBColor guess = new RYBColor(0.5f, 0.5f, 0.5f);
		// The function to minimize
		Function<RYBColor, Double> errorFunction = (color) -> { return color.toRGB(new RGBColor()).distSquared(rgb); };
		double epsilon = 0.0001, margin = 0.001, error;
		int maxIters = 50, iters = 0;
		// Keep adjusting guess until error is small enough
		while (true) {
			iters++;
			// Calculate error, stop if close enough
			error = errorFunction.apply(guess);
			if (error < margin) break;
			// To adjust guess, construct gradient by approximating partial derivatives along each axis
			RYBColor gradient = new RYBColor();
			for (Axis axis : Axis.values()) {
				// Create sample point
				RYBColor sample = guess.plus(axis.direction.scaledBy(epsilon));
				// Calculate error change in direction and add it to gradient
				double errorChange = (errorFunction.apply(sample) - error) / epsilon;
				gradient.add(axis.direction.scaledBy(errorChange));
			}
			// DEBUG
			// System.out.println(String.format("Guess: %s Error: %f Gradient: %s", guess, error, gradient));
			// Move guess by opposite of gradient
			// Break if maximum iterations reached
			guess.add(gradient.scaledBy(-0.3));
			if (iters == maxIters) break;
		}
		// Set ourselves to be the adjusted guess
		guess.clamp();
		this.setAll(guess);
		return this;
	}
	
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
	
}
