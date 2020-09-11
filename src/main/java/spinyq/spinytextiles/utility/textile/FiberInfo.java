package spinyq.spinytextiles.utility.textile;

import net.minecraft.nbt.CompoundNBT;
import spinyq.spinytextiles.utility.color.RYBKColor;

/**
 * Used to track info about the thread a player is spinning,
 * as well as fiber a player is adding to the spinning wheel.
 * @author Elijah Hilty
 *
 */
public class FiberInfo implements IGarmentComponent {
	
	private static final String TAG_COLOR = "Color", TAG_AMOUNT = "Amount";
	
	public RYBKColor color;
	public int amount; // Non-zero
	
	public FiberInfo() {
		
	}
	
	public FiberInfo(RYBKColor color, int amount) {
		super();
		this.color = color;
		this.amount = amount;
	}
	
	/**
	 * Copy method
	 */
	public FiberInfo copy() {
		return new FiberInfo(color.copy(), amount);
	}
	
	/**
	 * "Combines" another fiberinfo into a new one.
	 * Mixes color based on amounts.
	 * @param other
	 */
	public FiberInfo combine(FiberInfo other) {
		int totalAmount = amount + other.amount;
		// Mix color
		RYBKColor newColor = color.interp(other.color, (float) other.amount / (float) totalAmount);
		return new FiberInfo(newColor, totalAmount);
	}

	@Override
	public String toString() {
		return "FiberInfo [color=" + color + ", amount=" + amount + "]";
	}

	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT nbt = new CompoundNBT();
		nbt.putInt(TAG_COLOR, color.toInt());
		nbt.putInt(TAG_AMOUNT, amount);
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		color = new RYBKColor().fromInt(nbt.getInt(TAG_COLOR));
		amount = nbt.getInt(TAG_AMOUNT);
	}
	
	@Override
	public Type<?> getType() {
		return IGarmentComponent.FIBER;
	}
	
}