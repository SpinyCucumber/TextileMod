package spinyq.spinytextiles.utility.textile;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;
import spinyq.spinytextiles.utility.NBTHelper;
import spinyq.spinytextiles.utility.color.RYBKColor;

/**
 * Used to track info about the thread a player is spinning,
 * as well as fiber a player is adding to the spinning wheel.
 * @author Elijah Hilty
 *
 */
public class Fiber implements INBTSerializable<CompoundNBT> {
	
	private static final String COLOR_TAG = "Color", AMOUNT_TAG = "Amount";
	
	public RYBKColor color;
	public int amount; // Non-zero
	
	public Fiber() {
		
	}
	
	public Fiber(RYBKColor color, int amount) {
		super();
		this.color = color;
		this.amount = amount;
	}
	
	/**
	 * Copy method
	 */
	public Fiber copy() {
		return new Fiber(color.copy(), amount);
	}
	
	/**
	 * "Combines" another fiberinfo into a new one.
	 * Mixes color based on amounts.
	 * @param other
	 */
	public Fiber combine(Fiber other) {
		int totalAmount = amount + other.amount;
		// Mix color
		RYBKColor newColor = color.interp(other.color, (float) other.amount / (float) totalAmount);
		return new Fiber(newColor, totalAmount);
	}

	@Override
	public String toString() {
		return "FiberInfo [color=" + color + ", amount=" + amount + "]";
	}

	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT nbt = new CompoundNBT();
		NBTHelper.put(nbt, COLOR_TAG, color);
		nbt.putInt(AMOUNT_TAG, amount);
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		color = NBTHelper.get(nbt, COLOR_TAG, RYBKColor::new);
		amount = nbt.getInt(AMOUNT_TAG);
	}
	
}