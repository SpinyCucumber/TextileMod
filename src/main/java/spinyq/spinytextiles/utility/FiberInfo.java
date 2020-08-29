package spinyq.spinytextiles.utility;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;
import spinyq.spinytextiles.utility.color.ColorHelper;
import spinyq.spinytextiles.utility.color.RGBColor;

/**
 * Used to track info about the thread a player is spinning,
 * as well as fiber a player is adding to the spinning wheel.
 * TODO Move this
 * @author Elijah Hilty
 *
 */
public class FiberInfo implements INBTSerializable<CompoundNBT> {
	
	private static final String TAG_COLOR = "Color", TAG_AMOUNT = "Amount";
	
	public RGBColor color;
	public int amount; // Non-zero
	
	public FiberInfo() {
		
	}
	
	public FiberInfo(RGBColor color, int amount) {
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
		RGBColor newColor = ColorHelper.mixRealistic(color, other.color, (float) other.amount / (float) totalAmount);
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
		color = new RGBColor().fromInt(nbt.getInt(TAG_COLOR));
		amount = nbt.getInt(TAG_AMOUNT);
	}
	
}