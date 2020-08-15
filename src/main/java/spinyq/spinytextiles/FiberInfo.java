package spinyq.spinytextiles;

import spinyq.spinytextiles.utility.Color3f;
import spinyq.spinytextiles.utility.ColorHelper;

/**
 * Used to track info about the thread a player is spinning,
 * as well as fiber a player is adding to the spinning wheel.
 * TODO Move this
 * @author Elijah Hilty
 *
 */
public class FiberInfo {
	
	public Color3f color;
	public int amount; // Non-zero
	
	public FiberInfo(Color3f color, int amount) {
		super();
		this.color = color;
		this.amount = amount;
	}
	
	/**
	 * Copy constructor
	 */
	public FiberInfo(FiberInfo source) {
		this.color = new Color3f(source.color);
		this.amount = source.amount;
	}
	
	/**
	 * "Combines" another fiberinfo into a new one.
	 * Mixes color based on amounts.
	 * @param other
	 */
	public FiberInfo combine(FiberInfo other) {
		int totalAmount = amount + other.amount;
		// Mix color
		Color3f newColor = ColorHelper.mixRealistic(color, other.color, (float) other.amount / (float) amount);
		return new FiberInfo(newColor, totalAmount);
	}
	
}