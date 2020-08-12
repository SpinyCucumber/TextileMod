package spinyq.spinytextiles.items;

import net.minecraft.item.ItemStack;
import spinyq.spinytextiles.utility.Color3f;

public interface IFiberItem {

	Color3f getColor(ItemStack stack);
	int getFiberValue(ItemStack stack);
	
}
