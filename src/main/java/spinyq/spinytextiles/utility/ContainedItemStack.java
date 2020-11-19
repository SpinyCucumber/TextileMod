package spinyq.spinytextiles.utility;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

/**
 * Represents an ItemStack contained in an inventory.
 * 
 * @author Elijah Hilty
 *
 * @param <T> The inventory type
 */
public class ContainedItemStack<T extends IInventory> {

	private ItemStack stack;
	private T inventory;

	public ContainedItemStack(ItemStack stack, T inventory) {
		super();
		this.stack = stack;
		this.inventory = inventory;
	}

	public ItemStack getStack() {
		return stack;
	}

	public T getInventory() {
		return inventory;
	}

}
