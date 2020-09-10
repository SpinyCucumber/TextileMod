package spinyq.spinytextiles.items;

import net.minecraft.item.ItemStack;
import spinyq.spinytextiles.utility.textile.FiberInfo;

public interface IFiberItem {

	FiberInfo getInfo(ItemStack stack);
	
}
