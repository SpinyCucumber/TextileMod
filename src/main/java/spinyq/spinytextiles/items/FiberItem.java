package spinyq.spinytextiles.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import spinyq.spinytextiles.utility.Color3f;
import spinyq.spinytextiles.utility.FiberInfo;

public class FiberItem extends Item implements IFiberItem {

	private Color3f color;
	private int amountPerItem;
	
	public FiberItem(Properties properties, Color3f color, int amountPerItem) {
		super(properties);
		this.color = color;
		this.amountPerItem = amountPerItem;
	}

	@Override
	public FiberInfo getInfo(ItemStack stack) {
		return new FiberInfo(color, stack.getCount() * amountPerItem);
	}
	
}
