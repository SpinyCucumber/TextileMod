package spinyq.spinytextiles.utility;

public class ColorHelper {

	/**
	 * 
	 * @param base
	 * @param added
	 * @param amount A float from 0 to 1
	 * @return
	 */
	public static Color3f mixRealistic(Color3f base, Color3f added, float amount) {
		// TODO
		return base.lerp(added, amount);
	}
	
}
