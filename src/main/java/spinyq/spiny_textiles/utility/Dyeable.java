package spinyq.spiny_textiles.utility;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeItem;

public interface Dyeable<T, C> {

	void setColor(T object, C context, Color3f color);
	Color3f getColor(T object);
	
	int getDyeCost();
	
	public static interface DyeableItem extends IForgeItem, Dyeable<ItemStack, IInventory> { }
	
}
