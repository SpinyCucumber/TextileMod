package spinyq.spinytextiles.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import spinyq.spinytextiles.utility.FiberInfo;
import spinyq.spinytextiles.utility.color.RGBColor;

public class FiberItem extends Item implements IFiberItem {

	private RGBColor color;
	private int amountPerItem;
	
	public FiberItem(Properties properties, RGBColor color, int amountPerItem) {
		super(properties);
		this.color = color;
		this.amountPerItem = amountPerItem;
	}

	@Override
	public FiberInfo getInfo(ItemStack stack) {
		return new FiberInfo(color, stack.getCount() * amountPerItem);
	}
	
}
