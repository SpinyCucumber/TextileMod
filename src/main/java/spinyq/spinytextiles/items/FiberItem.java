package spinyq.spinytextiles.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import spinyq.spinytextiles.utility.color.RYBKColor;
import spinyq.spinytextiles.utility.textile.FiberInfo;

public class FiberItem extends Item implements IFiberItem {

	private RYBKColor color;
	private int amountPerItem;
	
	public FiberItem(Properties properties, RYBKColor color, int amountPerItem) {
		super(properties);
		this.color = color;
		this.amountPerItem = amountPerItem;
	}

	@Override
	public FiberInfo getInfo(ItemStack stack) {
		return new FiberInfo(color, stack.getCount() * amountPerItem);
	}
	
}
