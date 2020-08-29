package spinyq.spinytextiles.utility.color;

public class ColorHelper {

	/**
	 * 
	 * @param base
	 * @param added
	 * @param amount A float from 0 to 1
	 * @return
	 */
	public static RGBColor mixRealistic(RGBColor base, RGBColor added, float amount) {
		// TODO
		return base.interp(added, amount);
	}
	
}
