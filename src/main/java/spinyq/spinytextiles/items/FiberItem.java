package spinyq.spinytextiles.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import spinyq.spinytextiles.utility.color.RYBKColor;
import spinyq.spinytextiles.utility.textile.Fiber;

public class FiberItem extends Item {

	private RYBKColor color;
	private int amountPerItem;
	
	public FiberItem(Properties properties, RYBKColor color, int amountPerItem) {
		super(properties);
		this.color = color;
		this.amountPerItem = amountPerItem;
	}

	public Fiber getFiber(ItemStack stack) {
		return new Fiber(color, stack.getCount() * amountPerItem);
	}
	
}
