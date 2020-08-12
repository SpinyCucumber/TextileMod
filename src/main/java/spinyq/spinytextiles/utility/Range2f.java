package spinyq.spinytextiles.utility;

public class Range2f {

	private float a, b;

	public Range2f(float a, float b) {
		this.a = a;
		this.b = b;
	}
	
	public boolean contains(float f) {
		return (f > a) && (f < b);
	}
	
	/**
	 * Interpolates between the start and end of the range.
	 * @param s A value between 0.0 and 1.0
	 * @return
	 */
	public float lerp(float s) {
		return a + ((b - a) * s);
	}
	
}
