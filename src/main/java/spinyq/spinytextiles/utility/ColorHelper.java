package spinyq.spinytextiles.utility;

public class ColorHelper {

	public static Color3f mixRealistic(Color3f base, Color3f added, float amount) {
		// TODO
		return base.lerp(added, amount);
	}
	
}
