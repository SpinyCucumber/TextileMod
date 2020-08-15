package spinyq.spinytextiles.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import spinyq.spinytextiles.FiberInfo;

public class FiberItem extends Item implements IFiberItem {

	private FiberInfo info;
	
	public FiberItem(Properties properties, FiberInfo info) {
		super(properties);
		this.info = info;
	}

	@Override
	public FiberInfo getInfo(ItemStack stack) {
		return info;
	}
	
}
