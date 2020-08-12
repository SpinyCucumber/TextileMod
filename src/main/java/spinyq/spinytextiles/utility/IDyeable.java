package spinyq.spinytextiles.utility;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public interface IDyeable<T, C> {

	void setColor(T object, C context, Color3f color);
	Color3f getColor(T object);
	
	int getDyeCost();
	
	public static interface IDyeableItem extends IDyeable<ItemStack, IInventory> { }
	
}
